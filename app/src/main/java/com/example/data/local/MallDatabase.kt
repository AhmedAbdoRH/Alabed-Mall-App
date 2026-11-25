package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.EventEntity
import com.example.data.model.MallNotificationEntity
import com.example.data.model.OfferEntity
import com.example.data.model.ShoppingTaskEntity
import com.example.data.model.StoreEntity
import com.example.data.model.TripStopEntity
import com.example.data.model.VisitorTicketEntity

@Database(
    entities = [
        StoreEntity::class,
        OfferEntity::class,
        EventEntity::class,
        MallNotificationEntity::class,
        VisitorTicketEntity::class,
        TripStopEntity::class,
        ShoppingTaskEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class MallDatabase : RoomDatabase() {
    abstract fun mallDao(): MallDao

    companion object {
        @Volatile
        private var INSTANCE: MallDatabase? = null

        fun getInstance(context: Context): MallDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MallDatabase::class.java,
                    "el_abed_mall.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
