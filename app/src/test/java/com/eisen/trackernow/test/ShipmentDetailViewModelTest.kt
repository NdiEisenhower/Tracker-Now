package com.eisen.trackernow.test

import androidx.lifecycle.SavedStateHandle
import com.eisen.trackernow.domain.model.*
import com.eisen.trackernow.domain.usecase.GetShipmentDetailUseCase
import com.eisen.trackernow.presentation.viewmodel.ShipmentDetailViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShipmentDetailViewModelTest {

    private lateinit var viewModel: ShipmentDetailViewModel
    private lateinit var getShipmentDetailUseCase: GetShipmentDetailUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getShipmentDetailUseCase = mockk()
    }

    @Test
    fun `processShipmentDetail removes duplicates and sorts by most recent using real DHL shipment data`() = runTest {

        val shipment = Shipment(
            id = "shp_001",
            carrier = Carrier("dhl", "DHL Express"),
            trackingNumber = "JD7654321ZA",
            lastStatus = Status("OUT_FOR_DELIVERY", "Out for Delivery"),
            lastUpdatedAt = "2026-05-03T13:33:15.867Z",
            origin = Address("Johannesburg", "ZA"),
            destination = Address("Cape Town", "ZA"),
            estimatedDeliveryAt = "2026-05-03T18:00:00Z",
            isFavorite = false
        )
        val timelineWithDuplicates = listOf(
            StatusTimeline("2026-05-03T10:58:12.655Z", "IN_TRANSIT", "In Transit", "Cape Town"),
            StatusTimeline("2026-05-03T10:36:37.167Z", "IN_TRANSIT", "In Transit", "Cape Town"),
            StatusTimeline("2026-05-03T10:12:39.444Z", "IN_TRANSIT", "In Transit", "Cape Town"),
            StatusTimeline("2026-05-03T09:53:56.887Z", "IN_TRANSIT", "In transit", "Cape Town Depot"),
            StatusTimeline("2026-05-03T07:36:21.065Z", "IN_TRANSIT", "In transit", "Cape Town Depot"),
            StatusTimeline("2026-05-01T08:30:00Z", "IN_TRANSIT", "Departed sorting facility", "Johannesburg Hub, Germiston"),
            StatusTimeline("2026-04-30T22:15:00Z", "PROCESSED", "Processed at facility", "Johannesburg, OR Tambo Intl"),
            StatusTimeline("2026-04-30T14:00:00Z", "PICKED_UP", "Picked up by courier", "Sandton, Johannesburg"),
            StatusTimeline("2026-04-30T09:30:00Z", "LABEL_CREATED", "We have been notified of your parcel", "Seller, Johannesburg")
        )

        val originalDetail = ShipmentDetail(shipment, timelineWithDuplicates)

        coEvery { getShipmentDetailUseCase.invoke("shp_001") } returns originalDetail

        viewModel = ShipmentDetailViewModel(
            getShipmentDetailUseCase = getShipmentDetailUseCase,
            savedStateHandle = SavedStateHandle(mapOf("shipmentId" to "shp_001"))
        )

        val processedDetail = viewModel.processShipmentDetail(originalDetail)

        assertEquals(9, processedDetail.timeline.size)

        assertEquals("2026-05-03T10:58:12.655Z", processedDetail.timeline[0].time)
        assertEquals("In Transit", processedDetail.timeline[0].label)

        assertEquals("2026-05-03T10:36:37.167Z", processedDetail.timeline[1].time)
        assertEquals("2026-05-03T10:12:39.444Z", processedDetail.timeline[2].time)
        assertEquals("2026-05-03T09:53:56.887Z", processedDetail.timeline[3].time)
        assertEquals("2026-05-03T07:36:21.065Z", processedDetail.timeline[4].time)
        assertEquals("2026-05-01T08:30:00Z", processedDetail.timeline[5].time)
        assertEquals("2026-04-30T22:15:00Z", processedDetail.timeline[6].time)
        assertEquals("2026-04-30T14:00:00Z", processedDetail.timeline[7].time)
        assertEquals("2026-04-30T09:30:00Z", processedDetail.timeline[8].time)

        for (i in 0 until processedDetail.timeline.size - 1) {
            assertTrue(
                "Timeline at index $i should be newer than index ${i + 1}",
                processedDetail.timeline[i].time > processedDetail.timeline[i + 1].time
            )
        }
    }

    @Test
    fun `processShipmentDetail removes duplicate timeline entries from FedEx shipment`() = runTest {
        val shipment = Shipment(
            id = "shp_002",
            carrier = Carrier("fedex", "FedEx International"),
            trackingNumber = "FX8765432CN",
            lastStatus = Status("IN_TRANSIT", "In transit"),
            lastUpdatedAt = "2026-05-01T12:15:00Z",
            origin = Address("Shanghai", "CN"),
            destination = Address("Durban", "ZA"),
            estimatedDeliveryAt = "2026-05-05T14:00:00Z",
            isFavorite = false
        )

        val timeline = listOf(
            StatusTimeline("2026-05-01T12:15:00Z", "IN_TRANSIT", "Departed FedEx hub", "Dubai World Central, UAE"),
            StatusTimeline("2026-04-30T20:00:00Z", "IN_TRANSIT", "Arrived at transit facility", "Dubai, UAE"),
            StatusTimeline("2026-04-30T02:00:00Z", "EXPORT_CLEARED", "Export customs cleared", "Shanghai, CN"),
            StatusTimeline("2026-04-29T10:00:00Z", "PICKED_UP", "Picked up", "Shanghai, CN")
        )

        val originalDetail = ShipmentDetail(shipment, timeline)

        coEvery { getShipmentDetailUseCase.invoke("shp_002") } returns originalDetail

        viewModel = ShipmentDetailViewModel(
            getShipmentDetailUseCase = getShipmentDetailUseCase,
            savedStateHandle = SavedStateHandle(mapOf("shipmentId" to "shp_002"))
        )

        val processedDetail = viewModel.processShipmentDetail(originalDetail)

        assertEquals(4, processedDetail.timeline.size)
        assertTrue(processedDetail.timeline[0].time > processedDetail.timeline[1].time)
        assertTrue(processedDetail.timeline[1].time > processedDetail.timeline[2].time)
        assertTrue(processedDetail.timeline[2].time > processedDetail.timeline[3].time)
    }

    @Test
    fun `delivered shipment shows timeline with delivery as most recent`() = runTest {
        val shipment = Shipment(
            id = "shp_003",
            carrier = Carrier("aramex", "Aramex"),
            trackingNumber = "AR998877ZA",
            lastStatus = Status("DELIVERED", "Delivered"),
            lastUpdatedAt = "2026-04-30T16:45:00Z",
            origin = Address("Pretoria", "ZA"),
            destination = Address("Bloemfontein", "ZA"),
            estimatedDeliveryAt = "2026-04-30T11:00:00Z",
            isFavorite = false
        )

        val timeline = listOf(
            StatusTimeline("2026-04-30T11:00:00Z", "DELIVERED", "Delivered", "Bloemfontein, 9301"),
            StatusTimeline("2026-04-30T08:45:00Z", "OUT_FOR_DELIVERY", "Out for delivery", "Bloemfontein depot"),
            StatusTimeline("2026-04-29T15:30:00Z", "IN_TRANSIT", "Arrived at destination depot", "Bloemfontein"),
            StatusTimeline("2026-04-29T08:00:00Z", "PICKED_UP", "Picked up", "Pretoria, ZA")
        )

        val originalDetail = ShipmentDetail(shipment, timeline)

        coEvery { getShipmentDetailUseCase.invoke("shp_003") } returns originalDetail

        viewModel = ShipmentDetailViewModel(
            getShipmentDetailUseCase = getShipmentDetailUseCase,
            savedStateHandle = SavedStateHandle(mapOf("shipmentId" to "shp_003"))
        )

        val processedDetail = viewModel.processShipmentDetail(originalDetail)

        assertEquals(4, processedDetail.timeline.size)
        assertEquals("DELIVERED", processedDetail.timeline[0].code)
        assertEquals("OUT_FOR_DELIVERY", processedDetail.timeline[1].code)
    }
}