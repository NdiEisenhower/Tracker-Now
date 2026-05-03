package com.eisen.trackernow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eisen.trackernow.data.local.converters.Converters
import com.eisen.trackernow.data.local.dao.ShipmentDao
import com.eisen.trackernow.data.local.dao.StatusDao
import com.eisen.trackernow.data.local.entity.ShipmentEntity
import com.eisen.trackernow.data.local.entity.StatusTimelineEntity

@Database(
    entities = [ShipmentEntity::class, StatusTimelineEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ShipmentDatabase : RoomDatabase() {
    abstract fun shipmentDao(): ShipmentDao
    abstract fun statusDao(): StatusDao

    companion object {
        @Volatile
        private var INSTANCE: ShipmentDatabase? = null

        fun getDatabase(context: Context): ShipmentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShipmentDatabase::class.java,
                    "shipment_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}