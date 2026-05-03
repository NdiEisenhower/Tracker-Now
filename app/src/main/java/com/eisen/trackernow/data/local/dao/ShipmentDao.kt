package com.eisen.trackernow.data.local.dao

import androidx.room.*
import com.eisen.trackernow.data.local.entity.ShipmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShipmentDao {

    // Flow versions for reactive queries
    @Query("SELECT * FROM shipments ORDER BY lastUpdatedAt DESC")
    fun getAllShipments(): Flow<List<ShipmentEntity>>

    @Query("SELECT * FROM shipments WHERE isFavorite = 1 ORDER BY lastUpdatedAt DESC")
    fun getFavoriteShipments(): Flow<List<ShipmentEntity>>

    // Suspend versions for one-time queries
    @Query("SELECT * FROM shipments ORDER BY lastUpdatedAt DESC")
    suspend fun getAllShipmentsList(): List<ShipmentEntity>

    @Query("SELECT * FROM shipments WHERE id = :id")
    suspend fun getShipmentById(id: String): ShipmentEntity?

    @Query("SELECT * FROM shipments WHERE trackingNumber LIKE '%' || :query || '%' OR carrierName LIKE '%' || :query || '%'")
    suspend fun searchShipmentsList(query: String): List<ShipmentEntity>

    @Query("SELECT * FROM shipments WHERE lastStatusCode = :statusCode")
    suspend fun filterByStatusList(statusCode: String): List<ShipmentEntity>

    @Query("SELECT * FROM shipments WHERE isFavorite = 1 ORDER BY lastUpdatedAt DESC")
    suspend fun getFavoriteShipmentsList(): List<ShipmentEntity>

    @Query("SELECT * FROM shipments ORDER BY lastUpdatedAt DESC")
    suspend fun getCachedShipments(): List<ShipmentEntity>

    // Insert/Update/Delete operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shipment: ShipmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shipments: List<ShipmentEntity>)

    @Update
    suspend fun update(shipment: ShipmentEntity)

    @Delete
    suspend fun delete(shipment: ShipmentEntity)

    @Query("DELETE FROM shipments")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM shipments")
    suspend fun getCount(): Int

    @Query("UPDATE shipments SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM shipments WHERE lastCacheTime < :timestamp")
    suspend fun deleteOldShipments(timestamp: Long)
}