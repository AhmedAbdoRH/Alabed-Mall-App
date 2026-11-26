package com.example.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.NotificationPreferences
import com.example.data.model.EventEntity
import com.example.data.model.NotificationCategory
import com.example.data.model.OfferEntity
import com.example.data.model.SavedOfferEntity
import com.example.data.model.StoreEntity
import java.util.concurrent.TimeUnit

object MallNotificationScheduler {

    const val TAG_SAVED_OFFER_EXPIRY = "tag_saved_offer_expiry"
    const val TAG_INTEREST_OFFER = "tag_interest_offer"
    const val TAG_EVENT_DATE = "tag_event_date"

    fun savedOfferWorkName(offerId: String): String = "offer_expiry_$offerId"
    fun interestOfferWorkName(offerId: String): String = "interest_offer_$offerId"
    fun eventDateWorkName(eventId: String): String = "event_date_$eventId"

    private fun safeWorkManager(context: Context): WorkManager? {
        return try {
            WorkManager.getInstance(context.applicationContext)
        } catch (_: IllegalStateException) {
            null
        }
    }

    fun scheduleSavedOfferExpiryReminder(
        context: Context,
        savedOffer: SavedOfferEntity,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        val wm = safeWorkManager(context) ?: return false
        val delayMs = OfferScheduleDateHelper.computeSavedOfferReminderDelayMillis(
            expiresAtMillis = savedOffer.expiresAtMillis,
            nowMillis = nowMillis
        ) ?: return false

        val data = Data.Builder()
            .putString(MallScheduledNotificationWorker.KEY_KIND, MallScheduledNotificationWorker.KIND_SAVED_OFFER_EXPIRY)
            .putString(MallScheduledNotificationWorker.KEY_TITLE, "تذكير: عرض محفوظ قرب الانتهاء (${savedOffer.storeNameAr})")
            .putString(
                MallScheduledNotificationWorker.KEY_BODY,
                "ينتهي قريباً عرض «${savedOffer.titleAr}» (${OfferScheduleDateHelper.formatArabicDate(savedOffer.expiresAtMillis)}). لا تفوّت الاستفادة منه!"
            )
            .putString(MallScheduledNotificationWorker.KEY_TARGET_ID, savedOffer.storeId)
            .putString(MallScheduledNotificationWorker.KEY_LINKED_TYPE, "STORE")
            .putString(MallScheduledNotificationWorker.KEY_CATEGORY_NAME, NotificationCategory.OFFER.name)
            .putString(MallScheduledNotificationWorker.KEY_NOTIF_DB_ID, "expiry_${savedOffer.offerId}")
            .putInt(MallScheduledNotificationWorker.KEY_SYSTEM_NOTIF_ID, savedOffer.offerId.hashCode())
            .build()

        val request = OneTimeWorkRequestBuilder<MallScheduledNotificationWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(TAG_SAVED_OFFER_EXPIRY)
            .build()

        wm.enqueueUniqueWork(
            savedOfferWorkName(savedOffer.offerId),
            ExistingWorkPolicy.REPLACE,
            request
        )
        return true
    }

    fun cancelSavedOfferExpiryReminder(context: Context, offerId: String) {
        safeWorkManager(context)?.cancelUniqueWork(savedOfferWorkName(offerId))
    }

    fun syncInterestAndDateSchedules(
        context: Context,
        places: List<StoreEntity>,
        offers: List<OfferEntity>,
        events: List<EventEntity>,
        prefs: NotificationPreferences,
        nowMillis: Long = System.currentTimeMillis()
    ) {
        val wm = safeWorkManager(context) ?: return
        val placeMap = places.associateBy { it.id }

        // 1. Date-specific interest-matched offers
        offers.forEach { offer ->
            val storeCategory = placeMap[offer.storeId]?.categoryName
            val matchesInterest = prefs.selectedInterests.isEmpty() ||
                (storeCategory != null && storeCategory in prefs.selectedInterests)
            val delayMs = OfferScheduleDateHelper.computeInterestOfferAlertDelayMillis(offer, nowMillis)

            if (prefs.offersEnabled && matchesInterest && delayMs != null) {
                val data = Data.Builder()
                    .putString(MallScheduledNotificationWorker.KEY_KIND, MallScheduledNotificationWorker.KIND_INTEREST_OFFER)
                    .putString(MallScheduledNotificationWorker.KEY_TITLE, "عرض يناسب اهتماماتك: ${offer.storeNameAr}")
                    .putString(
                        MallScheduledNotificationWorker.KEY_BODY,
                        "${offer.titleAr} (${offer.discountBadge}) — كود: ${offer.couponCode}"
                    )
                    .putString(MallScheduledNotificationWorker.KEY_TARGET_ID, offer.storeId)
                    .putString(MallScheduledNotificationWorker.KEY_LINKED_TYPE, "STORE")
                    .putString(MallScheduledNotificationWorker.KEY_CATEGORY_NAME, NotificationCategory.OFFER.name)
                    .putString(MallScheduledNotificationWorker.KEY_REQUIRED_INTEREST, storeCategory)
                    .putString(MallScheduledNotificationWorker.KEY_NOTIF_DB_ID, "interest_${offer.id}")
                    .putInt(MallScheduledNotificationWorker.KEY_SYSTEM_NOTIF_ID, ("interest_${offer.id}").hashCode())
                    .build()

                val request = OneTimeWorkRequestBuilder<MallScheduledNotificationWorker>()
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag(TAG_INTEREST_OFFER)
                    .build()

                wm.enqueueUniqueWork(
                    interestOfferWorkName(offer.id),
                    ExistingWorkPolicy.KEEP,
                    request
                )
            } else {
                wm.cancelUniqueWork(interestOfferWorkName(offer.id))
            }
        }

        // 2. Date-specific events & occasions
        events.forEach { event ->
            val enabledForEvent = if (event.isNewOpening) prefs.openingsEnabled else prefs.eventsEnabled
            val delayMs = OfferScheduleDateHelper.computeEventReminderDelayMillis(event, nowMillis)

            if (enabledForEvent && delayMs != null) {
                val category = if (event.isNewOpening) NotificationCategory.OPENING else NotificationCategory.EVENT
                val data = Data.Builder()
                    .putString(MallScheduledNotificationWorker.KEY_KIND, MallScheduledNotificationWorker.KIND_EVENT_DATE)
                    .putString(MallScheduledNotificationWorker.KEY_TITLE, "موعد اليوم: ${event.titleAr}")
                    .putString(
                        MallScheduledNotificationWorker.KEY_BODY,
                        "${event.timeAr} • ${event.locationLabelAr}"
                    )
                    .putString(MallScheduledNotificationWorker.KEY_TARGET_ID, event.locationPlaceId)
                    .putString(MallScheduledNotificationWorker.KEY_LINKED_TYPE, "MAP")
                    .putString(MallScheduledNotificationWorker.KEY_CATEGORY_NAME, category.name)
                    .putString(MallScheduledNotificationWorker.KEY_NOTIF_DB_ID, "event_sched_${event.id}")
                    .putInt(MallScheduledNotificationWorker.KEY_SYSTEM_NOTIF_ID, ("event_sched_${event.id}").hashCode())
                    .build()

                val request = OneTimeWorkRequestBuilder<MallScheduledNotificationWorker>()
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag(TAG_EVENT_DATE)
                    .build()

                wm.enqueueUniqueWork(
                    eventDateWorkName(event.id),
                    ExistingWorkPolicy.KEEP,
                    request
                )
            } else {
                wm.cancelUniqueWork(eventDateWorkName(event.id))
            }
        }
    }
}
