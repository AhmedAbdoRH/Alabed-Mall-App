package com.example.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.MallDatabase
import com.example.data.local.NotificationPreferencesManager
import com.example.data.model.MallNotificationEntity
import com.example.data.model.NotificationCategory
import kotlinx.coroutines.flow.first

class MallScheduledNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val kind = inputData.getString(KEY_KIND) ?: KIND_SAVED_OFFER_EXPIRY
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val body = inputData.getString(KEY_BODY) ?: return Result.failure()
        val targetId = inputData.getString(KEY_TARGET_ID)
        val linkedType = inputData.getString(KEY_LINKED_TYPE) ?: "STORE"
        val categoryName = inputData.getString(KEY_CATEGORY_NAME) ?: NotificationCategory.OFFER.name
        val requiredInterest = inputData.getString(KEY_REQUIRED_INTEREST)
        val notifDbId = inputData.getString(KEY_NOTIF_DB_ID) ?: "scheduled_${System.currentTimeMillis()}"
        val systemNotifId = inputData.getInt(KEY_SYSTEM_NOTIF_ID, notifDbId.hashCode())

        val prefs = NotificationPreferencesManager(applicationContext).notificationPreferences.first()
        val category = NotificationCategory.entries.firstOrNull { it.name == categoryName }
            ?: NotificationCategory.OFFER

        // Respect user preferences for scheduled offer/event notifications
        val shouldDeliver = when (kind) {
            KIND_SAVED_OFFER_EXPIRY -> true // Explicit user-saved offer reminder
            KIND_INTEREST_OFFER -> {
                prefs.offersEnabled && (
                    prefs.selectedInterests.isEmpty() ||
                        (requiredInterest != null && requiredInterest in prefs.selectedInterests)
                    )
            }
            KIND_EVENT_DATE -> {
                when (category) {
                    NotificationCategory.OPENING -> prefs.openingsEnabled
                    NotificationCategory.EVENT -> prefs.eventsEnabled
                    else -> prefs.alertsEnabled
                }
            }
            else -> true
        }

        if (!shouldDeliver) {
            return Result.success()
        }

        MallNotificationHelper.showMallNotification(
            context = applicationContext,
            notificationId = systemNotifId,
            title = title,
            body = body
        )

        val dao = MallDatabase.getInstance(applicationContext).mallDao()
        dao.upsertNotification(
            MallNotificationEntity(
                id = notifDbId,
                titleAr = title,
                bodyAr = body,
                categoryName = category.name,
                timestampLabelAr = "مجدول",
                linkedTargetId = targetId,
                linkedType = linkedType,
                isRead = false
            )
        )
        return Result.success()
    }

    companion object {
        const val KEY_KIND = "kind"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_TARGET_ID = "target_id"
        const val KEY_LINKED_TYPE = "linked_type"
        const val KEY_CATEGORY_NAME = "category_name"
        const val KEY_REQUIRED_INTEREST = "required_interest"
        const val KEY_NOTIF_DB_ID = "notif_db_id"
        const val KEY_SYSTEM_NOTIF_ID = "system_notif_id"

        const val KIND_SAVED_OFFER_EXPIRY = "saved_offer_expiry"
        const val KIND_INTEREST_OFFER = "interest_offer"
        const val KIND_EVENT_DATE = "event_date"
    }
}
