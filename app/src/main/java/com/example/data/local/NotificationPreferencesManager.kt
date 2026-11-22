package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.MapMovementMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.mallPreferencesDataStore by preferencesDataStore(name = "el_abed_mall_prefs")

data class NotificationPreferences(
    val offersEnabled: Boolean = true,
    val openingsEnabled: Boolean = true,
    val eventsEnabled: Boolean = true,
    val alertsEnabled: Boolean = true
)

class NotificationPreferencesManager(private val context: Context) {

    private object Keys {
        val OFFERS_ENABLED = booleanPreferencesKey("notif_offers_enabled")
        val OPENINGS_ENABLED = booleanPreferencesKey("notif_openings_enabled")
        val EVENTS_ENABLED = booleanPreferencesKey("notif_events_enabled")
        val ALERTS_ENABLED = booleanPreferencesKey("notif_alerts_enabled")
        val DEFAULT_MAP_MODE = stringPreferencesKey("default_map_mode")
    }

    val notificationPreferences: Flow<NotificationPreferences> =
        context.mallPreferencesDataStore.data.map { prefs ->
            NotificationPreferences(
                offersEnabled = prefs[Keys.OFFERS_ENABLED] ?: true,
                openingsEnabled = prefs[Keys.OPENINGS_ENABLED] ?: true,
                eventsEnabled = prefs[Keys.EVENTS_ENABLED] ?: true,
                alertsEnabled = prefs[Keys.ALERTS_ENABLED] ?: true
            )
        }

    val defaultMapMode: Flow<MapMovementMode> =
        context.mallPreferencesDataStore.data.map { prefs ->
            val raw = prefs[Keys.DEFAULT_MAP_MODE]
            MapMovementMode.entries.firstOrNull { it.name == raw }
                ?: MapMovementMode.MANUAL_EXPLORE
        }

    suspend fun setOffersEnabled(enabled: Boolean) {
        context.mallPreferencesDataStore.edit { it[Keys.OFFERS_ENABLED] = enabled }
    }

    suspend fun setOpeningsEnabled(enabled: Boolean) {
        context.mallPreferencesDataStore.edit { it[Keys.OPENINGS_ENABLED] = enabled }
    }

    suspend fun setEventsEnabled(enabled: Boolean) {
        context.mallPreferencesDataStore.edit { it[Keys.EVENTS_ENABLED] = enabled }
    }

    suspend fun setAlertsEnabled(enabled: Boolean) {
        context.mallPreferencesDataStore.edit { it[Keys.ALERTS_ENABLED] = enabled }
    }

    suspend fun setDefaultMapMode(mode: MapMovementMode) {
        context.mallPreferencesDataStore.edit { it[Keys.DEFAULT_MAP_MODE] = mode.name }
    }
}
