package com.eisen.trackernow.domain.repository

import com.eisen.trackernow.data.PushUpdate
import com.eisen.trackernow.domain.model.Shipment
import com.eisen.trackernow.domain.model.ShipmentDetail
import com.eisen.trackernow.presentation.util.Resource
import kotlinx.coroutines.flow.Flow

interface ShipmentRepository {
    fun getShipmentsStream(): Flow<Resource<List<Shipment>>>
    suspend fun refreshShipments(): Result<Unit>
    suspend fun getShipmentDetail(id: String): ShipmentDetail?
    fun searchShipments(query: String): Flow<List<Shipment>>
    fun filterByStatus(statusCode: String): Flow<List<Shipment>>
    suspend fun toggleFavorite(shipmentId: String, isFavorite: Boolean): Result<Unit>
    suspend fun clearOldCache(daysOld: Int): Result<Unit>
    fun getFavoriteShipments(): Flow<List<Shipment>>
    fun getUserId(): String
    fun listenForUpdates(): Flow<PushUpdate>
    fun saveRefreshTime()
    fun getLastRefreshTime(): Long
    fun getLastUpdateTimestamp(): Long
    fun saveLastUpdateTimestamp(timestamp: Long)
}