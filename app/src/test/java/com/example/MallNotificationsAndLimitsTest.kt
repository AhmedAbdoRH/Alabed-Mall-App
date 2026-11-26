package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.data.local.NotificationPreferences
import com.example.data.local.NotificationPreferencesManager
import com.example.data.local.SeedCatalogData
import com.example.data.model.MallPlaceCategory
import com.example.data.model.SavedOfferEntity
import com.example.notifications.MallNotificationScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MallNotificationsAndLimitsTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val config = Configuration.Builder().build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun interests_picker_enforces_maximum_of_three_categories() = runBlocking {
        val manager = NotificationPreferencesManager(context)
        manager.setSelectedInterests(emptySet())

        assertTrue(manager.toggleInterest(MallPlaceCategory.HYPERMARKET.name))
        assertTrue(manager.toggleInterest(MallPlaceCategory.ELECTRONICS.name))
        assertTrue(manager.toggleInterest(MallPlaceCategory.DINING.name))

        // 4th category must be rejected
        assertFalse(manager.toggleInterest(MallPlaceCategory.FASHION.name))

        val afterFourth = manager.notificationPreferences.first().selectedInterests
        assertEquals(3, afterFourth.size)
        assertFalse(MallPlaceCategory.FASHION.name in afterFourth)

        // Removing one frees a slot for another
        assertTrue(manager.toggleInterest(MallPlaceCategory.DINING.name))
        assertTrue(manager.toggleInterest(MallPlaceCategory.FASHION.name))

        val updated = manager.notificationPreferences.first().selectedInterests
        assertEquals(3, updated.size)
        assertTrue(MallPlaceCategory.FASHION.name in updated)
    }

    @Test
    fun saved_offer_expiry_reminder_is_scheduled_and_cancelled_on_removal() {
        val now = 1_750_000_000_000L
        val threeDaysMs = 3L * 24L * 60L * 60L * 1000L
        val savedOffer = SavedOfferEntity(
            offerId = "offer_hyper_1",
            storeId = "hypermarket",
            storeNameAr = "هايبر ماركت العابد",
            titleAr = "مهرجان التوفير الأسبوعي",
            expiresAtMillis = now + threeDaysMs,
            savedAtMillis = now
        )

        val scheduled = MallNotificationScheduler.scheduleSavedOfferExpiryReminder(
            context = context,
            savedOffer = savedOffer,
            nowMillis = now
        )
        assertTrue(scheduled)

        val wm = WorkManager.getInstance(context)
        val uniqueName = MallNotificationScheduler.savedOfferWorkName(savedOffer.offerId)
        val enqueuedInfos = wm.getWorkInfosForUniqueWork(uniqueName).get()
        assertEquals(1, enqueuedInfos.size)
        assertEquals(WorkInfo.State.ENQUEUED, enqueuedInfos.first().state)

        MallNotificationScheduler.cancelSavedOfferExpiryReminder(context, savedOffer.offerId)
        val cancelledInfos = wm.getWorkInfosForUniqueWork(uniqueName).get()
        assertTrue(cancelledInfos.isEmpty() || cancelledInfos.first().state == WorkInfo.State.CANCELLED)
    }

    @Test
    fun sync_interest_and_date_schedules_respects_selected_interests_and_event_dates() {
        val now = System.currentTimeMillis()
        val prefs = NotificationPreferences(
            offersEnabled = true,
            openingsEnabled = true,
            eventsEnabled = true,
            alertsEnabled = true,
            selectedInterests = setOf(MallPlaceCategory.HYPERMARKET.name)
        )

        MallNotificationScheduler.syncInterestAndDateSchedules(
            context = context,
            places = SeedCatalogData.stores,
            offers = SeedCatalogData.offers,
            events = SeedCatalogData.events,
            prefs = prefs,
            nowMillis = now
        )

        val wm = WorkManager.getInstance(context)

        val matchingOfferWork = wm.getWorkInfosForUniqueWork(
            MallNotificationScheduler.interestOfferWorkName("offer_hyper_1")
        ).get()
        assertEquals(1, matchingOfferWork.size)
        assertEquals(WorkInfo.State.ENQUEUED, matchingOfferWork.first().state)

        val nonMatchingOfferWork = wm.getWorkInfosForUniqueWork(
            MallNotificationScheduler.interestOfferWorkName("offer_samsung_2")
        ).get()
        assertTrue(nonMatchingOfferWork.isEmpty() || nonMatchingOfferWork.first().state == WorkInfo.State.CANCELLED)

        val eventWork = wm.getWorkInfosForUniqueWork(
            MallNotificationScheduler.eventDateWorkName("event_opening_oud")
        ).get()
        assertEquals(1, eventWork.size)
        assertEquals(WorkInfo.State.ENQUEUED, eventWork.first().state)
    }
}
