package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.EventEntity
import com.example.data.model.MallNotificationEntity
import com.example.data.model.OfferEntity
import com.example.data.model.StoreEntity
import com.example.data.model.VisitorTicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MallDao {
    @Query("SELECT * FROM stores ORDER BY isServiceFacility ASC, floor ASC, nameAr ASC")
    fun observeAllPlaces(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE id = :id LIMIT 1")
    fun observePlaceById(id: String): Flow<StoreEntity?>

    @Query("SELECT COUNT(*) FROM stores")
    suspend fun getPlacesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStores(stores: List<StoreEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStore(store: StoreEntity)

    @Query("DELETE FROM stores WHERE id = :id")
    suspend fun deleteStoreById(id: String)

    @Query("SELECT * FROM offers ORDER BY isFeatured DESC, id ASC")
    fun observeAllOffers(): Flow<List<OfferEntity>>

    @Query("SELECT * FROM offers WHERE storeId = :storeId")
    fun observeOffersForStore(storeId: String): Flow<List<OfferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOffers(offers: List<OfferEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOffer(offer: OfferEntity)

    @Query("DELETE FROM offers WHERE id = :id")
    suspend fun deleteOfferById(id: String)

    @Query("SELECT * FROM events ORDER BY isNewOpening DESC, id ASC")
    fun observeAllEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvents(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: String)

    @Query("SELECT * FROM notifications ORDER BY isRead ASC, id DESC")
    fun observeAllNotifications(): Flow<List<MallNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotifications(notifications: List<MallNotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNotification(notification: MallNotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsRead()

    @Query("SELECT * FROM visitor_tickets ORDER BY createdAtMillis DESC")
    fun observeAllTickets(): Flow<List<VisitorTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: VisitorTicketEntity)
}
