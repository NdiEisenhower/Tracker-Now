package com.eisen.trackernow.test

import com.eisen.trackernow.data.remote.MockShipmentApi
import com.eisen.trackernow.data.repository.ShipmentRepositoryImpl
import com.eisen.trackernow.presentation.util.Resource
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShipmentRepositoryTest {

    private lateinit var repository: ShipmentRepositoryImpl
    private lateinit var shipmentDao: MockShipmentDao
    private lateinit var api: MockShipmentApi
    private lateinit var networkMonitor: TestNetworkMonitor
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        shipmentDao = MockShipmentDao()
        api = MockShipmentApi()
        networkMonitor = TestNetworkMonitor()

        repository = ShipmentRepositoryImpl(
            shipmentDao = shipmentDao,
            statusDao = mockk(),
            api = api,
            networkMonitor = networkMonitor,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `getShipmentsStream returns cached data first then fresh data`() = runTest {
        // Given
        val cachedShipment = createTestShipmentEntity(id = "cached_1")
        shipmentDao.insert(cachedShipment)

        val freshShipment = createTestShipmentEntity(id = "fresh_1")
        api.setMockResponse(listOf(freshShipment))

        // When
        val results = mutableListOf<Resource<List<Shipment>>>()
        repository.getShipmentsStream().toList(results)

        // Then
        assert(results[0] is Resource.Success)
        assert((results[0] as Resource.Success).data.first().id == "cached_1")

        advanceUntilIdle()

        assert(results[1] is Resource.Success)
        assert((results[1] as Resource.Success).data.first().id == "fresh_1")
    }

    @Test
    fun `refreshShipments updates cache when online`() = runTest {
        // Given
        networkMonitor.setConnected(true)
        val shipment = createTestShipmentEntity()
        api.setMockResponse(listOf(shipment))

        // When
        val result = repository.refreshShipments()

        // Then
        assert(result.isSuccess)
        val cached = shipmentDao.getAllShipments()
        assert(cached.isNotEmpty())
    }

    @Test
    fun `refreshShipments returns error when offline`() = runTest {
        // Given
        networkMonitor.setConnected(false)

        // When
        val result = repository.refreshShipments()

        // Then
        assert(result.isFailure)
    }
}