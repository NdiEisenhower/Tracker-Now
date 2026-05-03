package com.eisen.trackernow.data.repository

import com.eisen.trackernow.data.PushUpdate
import com.eisen.trackernow.data.local.dao.ShipmentDao
import com.eisen.trackernow.data.local.dao.StatusDao
import com.eisen.trackernow.data.local.entity.toDomain
import com.eisen.trackernow.data.local.entity.toEntity
import com.eisen.trackernow.data.remote.PushUpdateRepository
import com.eisen.trackernow.data.remote.ShipmentApi
import com.eisen.trackernow.di.IoDispatcher
import com.eisen.trackernow.domain.model.Shipment
import com.eisen.trackernow.domain.model.ShipmentDetail
import com.eisen.trackernow.domain.repository.NetworkMonitor
import com.eisen.trackernow.domain.repository.ShipmentRepository
import com.eisen.trackernow.presentation.util.Resource
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException

@Singleton
class ShipmentRepositoryImpl @Inject constructor(
    private val shipmentDao: ShipmentDao,
    private val statusDao: StatusDao,
    private val api: ShipmentApi,
    private val networkMonitor: NetworkMonitor,
    private val pushUpdateRepository: PushUpdateRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ShipmentRepository {

    override fun getShipmentsStream(): Flow<Resource<List<Shipment>>> {
        return flow {
            emit(Resource.Loading)

            // First, emit cached data if available
            var hasCachedData = false
            var cachedShipments = emptyList<Shipment>()

            try {
                val cachedEntities = withContext(ioDispatcher) {
                    shipmentDao.getCachedShipments()
                }
                if (cachedEntities.isNotEmpty()) {
                    hasCachedData = true
                    cachedShipments = cachedEntities.map { it.toDomain() }
                    emit(Resource.Success(cachedShipments))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Then try to fetch fresh data from Firebase REST API
            if (networkMonitor.isConnected()) {
                try {
                    val shipmentsMap = withContext(ioDispatcher) {
                        api.getShipments()
                    }

                    // Convert Map to List and add IDs
                    val shipments = shipmentsMap.map { (id, shipmentDto) ->
                        shipmentDto.copy(id = id).toEntity()
                    }.toList()

                    withContext(ioDispatcher) {
                        // 1. Save basic shipment info
                        shipmentDao.insertAll(shipments)

                        // 2. Fetch and cache details for each shipment (optional, for offline view)
                        shipments.forEach { shipment ->
                            try {
                                val detailResponse = api.getShipmentDetail(shipment.id)
                                val statuses = detailResponse.statuses.map { status ->
                                    status.toEntity(shipment.id)
                                }
                                if (statuses.isNotEmpty()) {
                                    statusDao.insertAll(statuses)
                                }
                            } catch (e: Exception) {
                                // Don't fail the whole operation if one detail fails
                                e.printStackTrace()
                            }
                        }
                    }

                    emit(Resource.Success(shipments.map { it.toDomain() }))

                } catch (e: Exception) {
                    if (!hasCachedData) {
                        emit(Resource.Error("Failed to load shipments: ${e.message}", e))
                    } else {
                        emit(Resource.Success(cachedShipments, offline = true))
                    }
                }

            } else {
                if (!hasCachedData) {
                    emit(Resource.Error("No internet connection and no cached data available"))
                } else {
                    emit(Resource.Success(cachedShipments, offline = true))
                }
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun refreshShipments(): Result<Unit> = withContext(ioDispatcher) {
        return@withContext try {
            if (networkMonitor.isConnected()) {
                val shipmentsMap = api.getShipments() // Fixed: was getAllShipments()
                val shipments = shipmentsMap.map { (id, shipmentDto) ->
                    shipmentDto.copy(id = id).toEntity()
                }.toList()

                shipmentDao.insertAll(shipments)

                shipments.forEach { shipment ->
                    try {
                        val detailResponse = api.getShipmentDetail(shipment.id) // Fixed: was firebaseApi
                        val statuses = detailResponse.statuses.map { status ->
                            status.toEntity(shipment.id)
                        }
                        if (statuses.isNotEmpty()) {
                            statusDao.insertAll(statuses)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                saveRefreshTime()
                Result.success(Unit)
            } else {
                Result.failure(IOException("No internet connection"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getShipmentDetail(id: String): ShipmentDetail? = withContext(ioDispatcher) {
        try {
            // Try to get from cache first
            val shipmentEntity = shipmentDao.getShipmentById(id)
            val statusEntities = statusDao.getStatusesForShipmentSync(id)

            if (shipmentEntity != null && statusEntities.isNotEmpty()) {
                return@withContext ShipmentDetail(
                    shipment = shipmentEntity.toDomain(),
                    timeline = statusEntities.map { it.toDomain() }
                )
            } else if (shipmentEntity != null) {
                return@withContext ShipmentDetail(
                    shipment = shipmentEntity.toDomain(),
                    timeline = emptyList()
                )
            }

            // If not in cache, fetch from network
            if (networkMonitor.isConnected()) {
                try {
                    val response = api.getShipmentDetail(id) // Fixed: was firebaseApi

                    // Convert to entity with the provided ID
                    val shipmentEntityFromResponse = response.toEntity(id)
                    shipmentDao.insert(shipmentEntityFromResponse)

                    val statusEntitiesFromResponse = response.statuses.map { it.toEntity(id) }
                    if (statusEntitiesFromResponse.isNotEmpty()) {
                        statusDao.insertAll(statusEntitiesFromResponse)
                    }

                    return@withContext ShipmentDetail(
                        shipment = shipmentEntityFromResponse.toDomain(),
                        timeline = response.statuses.map { it.toDomain() }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@withContext null
                }
            }

            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    override fun searchShipments(query: String): Flow<List<Shipment>> = flow {
        val shipments = if (query.isBlank()) {
            withContext(ioDispatcher) {
                shipmentDao.getAllShipmentsList()
            }
        } else {
            withContext(ioDispatcher) {
                shipmentDao.searchShipmentsList(query)
            }
        }
        emit(shipments.map { it.toDomain() })
    }.flowOn(ioDispatcher)

    override fun filterByStatus(statusCode: String): Flow<List<Shipment>> = flow {
        val shipments = if (statusCode == "ALL") {
            withContext(ioDispatcher) {
                shipmentDao.getAllShipmentsList()
            }
        } else {
            withContext(ioDispatcher) {
                shipmentDao.filterByStatusList(statusCode)
            }
        }
        emit(shipments.map { it.toDomain() })
    }.flowOn(ioDispatcher)

    override suspend fun toggleFavorite(shipmentId: String, isFavorite: Boolean): Result<Unit> {
        return try {
            shipmentDao.updateFavorite(shipmentId, isFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearOldCache(daysOld: Int): Result<Unit> {
        return try {
            val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
            shipmentDao.deleteOldShipments(cutoffTime)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getFavoriteShipments(): Flow<List<Shipment>> = flow {
        val favorites = withContext(ioDispatcher) {
            shipmentDao.getFavoriteShipmentsList()
        }
        emit(favorites.map { it.toDomain() })
    }.flowOn(ioDispatcher)

    // Push notification methods
    override fun getUserId(): String = runBlocking { pushUpdateRepository.getUserId() }
    override fun listenForUpdates(): Flow<PushUpdate> = pushUpdateRepository.listenForUpdates(getUserId())
    override fun saveRefreshTime() = pushUpdateRepository.saveRefreshTime()
    override fun getLastRefreshTime(): Long = pushUpdateRepository.getLastRefreshTime()
    override fun getLastUpdateTimestamp(): Long = pushUpdateRepository.getLastUpdateTimestamp()
    override fun saveLastUpdateTimestamp(timestamp: Long) = pushUpdateRepository.saveLastUpdateTimestamp(timestamp)
}