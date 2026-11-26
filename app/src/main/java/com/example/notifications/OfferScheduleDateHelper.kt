package com.example.notifications

import com.example.data.model.EventEntity
import com.example.data.model.OfferEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object OfferScheduleDateHelper {

    const val ONE_DAY_MS = 24L * 60L * 60L * 1000L
    const val ONE_HOUR_MS = 60L * 60L * 1000L

    fun resolveExpiryMillis(
        offer: OfferEntity,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        if (offer.expiresAtMillis > nowMillis) {
            return offer.expiresAtMillis
        }
        return nowMillis + 7L * ONE_DAY_MS
    }

    /**
     * Schedules 24h before expiry; if expiry is less than 24h away, falls back to
     * 1 hour before expiry (or at least 1 minute from now) so user still gets a reminder.
     */
    fun computeSavedOfferReminderDelayMillis(
        expiresAtMillis: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): Long? {
        if (expiresAtMillis <= nowMillis) return null
        val oneDayBefore = expiresAtMillis - ONE_DAY_MS
        if (oneDayBefore > nowMillis) {
            return oneDayBefore - nowMillis
        }
        val oneHourBefore = expiresAtMillis - ONE_HOUR_MS
        return if (oneHourBefore > nowMillis) {
            oneHourBefore - nowMillis
        } else {
            (expiresAtMillis - nowMillis).coerceAtLeast(60_000L)
        }
    }

    /**
     * Schedules an upcoming date-specific reminder for offers matching user's interests.
     * Targets the next morning (10:00 AM) before the offer expires, or 12h ahead if sooner.
     */
    fun computeInterestOfferAlertDelayMillis(
        offer: OfferEntity,
        nowMillis: Long = System.currentTimeMillis()
    ): Long? {
        val expiry = resolveExpiryMillis(offer, nowMillis)
        if (expiry <= nowMillis) return null

        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val targetMillis = if (cal.timeInMillis < expiry) cal.timeInMillis else (nowMillis + 6L * ONE_HOUR_MS).coerceAtMost(expiry - 60_000L)
        val delay = targetMillis - nowMillis
        return if (delay > 0L) delay else 60_000L
    }

    /**
     * Schedules a local notification at 10:00 AM on the day of the event/occasion (or 2h before if same day).
     */
    fun computeEventReminderDelayMillis(
        event: EventEntity,
        nowMillis: Long = System.currentTimeMillis()
    ): Long? {
        val scheduled = event.scheduledDateMillis
        if (scheduled <= nowMillis) return null

        val cal = Calendar.getInstance().apply {
            timeInMillis = scheduled
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val reminderAt = if (cal.timeInMillis > nowMillis) cal.timeInMillis else scheduled
        val delay = reminderAt - nowMillis
        return if (delay > 0L) delay else 60_000L
    }

    fun formatArabicDate(millis: Long): String {
        if (millis <= 0L) return "غير محدد"
        return SimpleDateFormat("d MMMM yyyy", Locale("ar")).format(Date(millis))
    }
}
