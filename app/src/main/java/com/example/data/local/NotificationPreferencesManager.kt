package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.MapMovementMode
import com.example.data.model.PromoBannerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.mallPreferencesDataStore by preferencesDataStore(name = "el_abed_mall_prefs")

data class NotificationPreferences(
    val offersEnabled: Boolean = true,
    val openingsEnabled: Boolean = true,
    val eventsEnabled: Boolean = true,
    val alertsEnabled: Boolean = true,
    val selectedInterests: Set<String> = emptySet()
)

class NotificationPreferencesManager(private val context: Context) {

    private object Keys {
        val OFFERS_ENABLED = booleanPreferencesKey("notif_offers_enabled")
        val OPENINGS_ENABLED = booleanPreferencesKey("notif_openings_enabled")
        val EVENTS_ENABLED = booleanPreferencesKey("notif_events_enabled")
        val ALERTS_ENABLED = booleanPreferencesKey("notif_alerts_enabled")
        val SELECTED_INTERESTS = stringSetPreferencesKey("visitor_selected_interests")
        val DEFAULT_MAP_MODE = stringPreferencesKey("default_map_mode")

        val PROMO_IMAGE = stringPreferencesKey("promo_image_path")
        val PROMO_TITLE = stringPreferencesKey("promo_title_ar")
        val PROMO_SUBTITLE = stringPreferencesKey("promo_subtitle_ar")
        val PROMO_BADGE = stringPreferencesKey("promo_badge_ar")
        val PROMO_TARGET_ID = stringPreferencesKey("promo_target_id")
        val PROMO_VISIBLE = booleanPreferencesKey("promo_visible")
    }

    val notificationPreferences: Flow<NotificationPreferences> =
        context.mallPreferencesDataStore.data.map { prefs ->
            NotificationPreferences(
                offersEnabled = prefs[Keys.OFFERS_ENABLED] ?: true,
                openingsEnabled = prefs[Keys.OPENINGS_ENABLED] ?: true,
                eventsEnabled = prefs[Keys.EVENTS_ENABLED] ?: true,
                alertsEnabled = prefs[Keys.ALERTS_ENABLED] ?: true,
                selectedInterests = prefs[Keys.SELECTED_INTERESTS] ?: emptySet()
            )
        }

    val defaultMapMode: Flow<MapMovementMode> =
        context.mallPreferencesDataStore.data.map { prefs ->
            val raw = prefs[Keys.DEFAULT_MAP_MODE]
            MapMovementMode.entries.firstOrNull { it.name == raw }
                ?: MapMovementMode.MANUAL_EXPLORE
        }

    val promoBannerConfig: Flow<PromoBannerConfig> =
        context.mallPreferencesDataStore.data.map { prefs ->
            val defaults = PromoBannerConfig()
            PromoBannerConfig(
                imagePathOrKey = prefs[Keys.PROMO_IMAGE] ?: defaults.imagePathOrKey,
                titleAr = prefs[Keys.PROMO_TITLE] ?: defaults.titleAr,
                subtitleAr = prefs[Keys.PROMO_SUBTITLE] ?: defaults.subtitleAr,
                badgeAr = prefs[Keys.PROMO_BADGE] ?: defaults.badgeAr,
                linkedTargetId = prefs[Keys.PROMO_TARGET_ID] ?: defaults.linkedTargetId,
                isVisible = prefs[Keys.PROMO_VISIBLE] ?: defaults.isVisible
            )
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

    suspend fun toggleInterest(categoryName: String) {
        context.mallPreferencesDataStore.edit { prefs ->
            val current = prefs[Keys.SELECTED_INTERESTS]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(categoryName)) {
                current.remove(categoryName)
            }
            prefs[Keys.SELECTED_INTERESTS] = current
        }
    }

    suspend fun setSelectedInterests(interests: Set<String>) {
        context.mallPreferencesDataStore.edit { it[Keys.SELECTED_INTERESTS] = interests }
    }

    suspend fun setDefaultMapMode(mode: MapMovementMode) {
        context.mallPreferencesDataStore.edit { it[Keys.DEFAULT_MAP_MODE] = mode.name }
    }

    suspend fun savePromoBanner(config: PromoBannerConfig) {
        context.mallPreferencesDataStore.edit { prefs ->
            prefs[Keys.PROMO_IMAGE] = config.imagePathOrKey
            prefs[Keys.PROMO_TITLE] = config.titleAr
            prefs[Keys.PROMO_SUBTITLE] = config.subtitleAr
            prefs[Keys.PROMO_BADGE] = config.badgeAr
            prefs[Keys.PROMO_TARGET_ID] = config.linkedTargetId
            prefs[Keys.PROMO_VISIBLE] = config.isVisible
        }
    }

    suspend fun resetPromoBanner() {
        val defaults = PromoBannerConfig()
        savePromoBanner(defaults)
    }
}
