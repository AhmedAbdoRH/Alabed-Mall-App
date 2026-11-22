package com.example.data.repository

import com.example.data.local.MallDao
import com.example.data.local.NotificationPreferences
import com.example.data.local.NotificationPreferencesManager
import com.example.data.local.SeedCatalogData
import com.example.data.model.EventEntity
import com.example.data.model.MallNotificationEntity
import com.example.data.model.MapMovementMode
import com.example.data.model.OfferEntity
import com.example.data.model.StoreEntity
import com.example.data.model.VisitorTicketEntity
import kotlinx.coroutines.flow.Flow

class MallRepository(
    private val dao: MallDao,
    private val prefsManager: NotificationPreferencesManager
) {
    val places: Flow<List<StoreEntity>> = dao.observeAllPlaces()
    val offers: Flow<List<OfferEntity>> = dao.observeAllOffers()
    val events: Flow<List<EventEntity>> = dao.observeAllEvents()
    val notifications: Flow<List<MallNotificationEntity>> = dao.observeAllNotifications()
    val tickets: Flow<List<VisitorTicketEntity>> = dao.observeAllTickets()
    val notificationPreferences: Flow<NotificationPreferences> = prefsManager.notificationPreferences
    val defaultMapMode: Flow<MapMovementMode> = prefsManager.defaultMapMode

    fun observePlace(id: String): Flow<StoreEntity?> = dao.observePlaceById(id)
    fun observeOffersForStore(storeId: String): Flow<List<OfferEntity>> = dao.observeOffersForStore(storeId)

    suspend fun ensureSeeded() {
        if (dao.getPlacesCount() == 0) {
            dao.upsertStores(SeedCatalogData.stores)
            dao.upsertOffers(SeedCatalogData.offers)
            dao.upsertEvents(SeedCatalogData.events)
            dao.upsertNotifications(SeedCatalogData.notifications)
        }
    }

    suspend fun upsertStore(store: StoreEntity) = dao.upsertStore(store)
    suspend fun deleteStore(id: String) = dao.deleteStoreById(id)

    suspend fun upsertOffer(offer: OfferEntity) = dao.upsertOffer(offer)
    suspend fun deleteOffer(id: String) = dao.deleteOfferById(id)

    suspend fun upsertEvent(event: EventEntity) = dao.upsertEvent(event)
    suspend fun deleteEvent(id: String) = dao.deleteEventById(id)

    suspend fun addNotification(notification: MallNotificationEntity) = dao.upsertNotification(notification)
    suspend fun markNotificationRead(id: String) = dao.markNotificationRead(id)
    suspend fun markAllNotificationsRead() = dao.markAllNotificationsRead()

    suspend fun submitVisitorTicket(ticket: VisitorTicketEntity) = dao.insertTicket(ticket)

    suspend fun setOffersNotifEnabled(enabled: Boolean) = prefsManager.setOffersEnabled(enabled)
    suspend fun setOpeningsNotifEnabled(enabled: Boolean) = prefsManager.setOpeningsEnabled(enabled)
    suspend fun setEventsNotifEnabled(enabled: Boolean) = prefsManager.setEventsEnabled(enabled)
    suspend fun setAlertsNotifEnabled(enabled: Boolean) = prefsManager.setAlertsEnabled(enabled)
    suspend fun setDefaultMapMode(mode: MapMovementMode) = prefsManager.setDefaultMapMode(mode)
}
