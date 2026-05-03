package com.eisen.trackernow.data.local.dao

import androidx.room.*
import com.eisen.trackernow.data.local.entity.StatusTimelineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusDao {

    @Query("SELECT * FROM status_timeline WHERE shipmentId = :shipmentId ORDER BY time DESC")
    fun getStatusesForShipment(shipmentId: String): Flow<List<StatusTimelineEntity>>

    @Query("SELECT * FROM status_timeline WHERE shipmentId = :shipmentId ORDER BY time DESC")
    suspend fun getStatusesForShipmentSync(shipmentId: String): List<StatusTimelineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(status: StatusTimelineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(statuses: List<StatusTimelineEntity>)

    @Query("DELETE FROM status_timeline WHERE shipmentId = :shipmentId")
    suspend fun deleteStatusesForShipment(shipmentId: String)

    @Query("SELECT DISTINCT shipmentId FROM status_timeline")
    suspend fun getShipmentsWithStatusHistory(): List<String>

    @Query("SELECT MIN(time) FROM status_timeline WHERE shipmentId = :shipmentId")
    suspend fun getEarliestStatusTime(shipmentId: String): String?
}