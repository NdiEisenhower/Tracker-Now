package com.eisen.trackernow.test

import com.eisen.trackernow.data.remote.dto.ShipmentDetailResponse
import com.eisen.trackernow.data.remote.dto.ShipmentDto
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ShipmentDtoTest {

    private lateinit var moshi: Moshi
    private lateinit var shipmentAdapter: JsonAdapter<ShipmentDto>
    private lateinit var detailAdapter: JsonAdapter<ShipmentDetailResponse>

    @Before
    fun setup() {
        moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        shipmentAdapter = moshi.adapter(ShipmentDto::class.java)
        detailAdapter = moshi.adapter(ShipmentDetailResponse::class.java)
    }

    @Test
    fun `shipment DTO parses DHL shipment correctly from JSON`() {
        val json = """
        {
            "carrier": {
                "code": "dhl",
                "name": "DHL Express"
            },
            "destination": {
                "city": "Cape Town",
                "country": "ZA"
            },
            "estimatedDeliveryAt": "2026-05-03T18:00:00Z",
            "id": "shp_001",
            "lastStatus": {
                "code": "OUT_FOR_DELIVERY",
                "label": "Out for Delivery"
            },
            "lastUpdatedAt": "2026-05-03T13:33:15.867Z",
            "origin": {
                "city": "Johannesburg",
                "country": "ZA"
            },
            "trackingNumber": "JD7654321ZA"
        }
        """.trimIndent()

        val shipment = shipmentAdapter.fromJson(json)

        assertNotNull(shipment)
        assertEquals("shp_001", shipment?.id)
        assertEquals("dhl", shipment?.carrier?.code)
        assertEquals("DHL Express", shipment?.carrier?.name)
        assertEquals("JD7654321ZA", shipment?.trackingNumber)
        assertEquals("OUT_FOR_DELIVERY", shipment?.lastStatus?.code)
        assertEquals("Out for Delivery", shipment?.lastStatus?.label)
        assertEquals("2026-05-03T13:33:15.867Z", shipment?.lastUpdatedAt)
        assertEquals("Johannesburg", shipment?.origin?.city)
        assertEquals("ZA", shipment?.origin?.country)
        assertEquals("Cape Town", shipment?.destination?.city)
        assertEquals("ZA", shipment?.destination?.country)
        assertEquals("2026-05-03T18:00:00Z", shipment?.estimatedDeliveryAt)
    }

    @Test
    fun `shipment detail DTO parses DHL timeline correctly with all nested data`() {
        val json = """
        {
            "carrier": {
                "code": "dhl",
                "name": "DHL Express"
            },
            "destination": {
                "city": "Cape Town",
                "country": "ZA"
            },
            "estimatedDeliveryAt": "2026-05-03T18:00:00Z",
            "id": "shp_001",
            "origin": {
                "city": "Johannesburg",
                "country": "ZA"
            },
            "statuses": [
                {
                    "code": "IN_TRANSIT",
                    "label": "Departed sorting facility",
                    "location": "Johannesburg Hub, Germiston",
                    "time": "2026-05-01T08:30:00Z"
                },
                {
                    "code": "PROCESSED",
                    "label": "Processed at facility",
                    "location": "Johannesburg, OR Tambo Intl",
                    "time": "2026-04-30T22:15:00Z"
                },
                {
                    "code": "PICKED_UP",
                    "label": "Picked up by courier",
                    "location": "Sandton, Johannesburg",
                    "time": "2026-04-30T14:00:00Z"
                },
                {
                    "code": "LABEL_CREATED",
                    "label": "We have been notified of your parcel",
                    "location": "Seller, Johannesburg",
                    "time": "2026-04-30T09:30:00Z"
                }
            ],
            "trackingNumber": "JD7654321ZA"
        }
        """.trimIndent()

        val detail = detailAdapter.fromJson(json)

        assertNotNull(detail)
        assertEquals("shp_001", detail?.id)
        assertEquals("dhl", detail?.carrier?.code)
        assertEquals("JD7654321ZA", detail?.trackingNumber)

        val statuses = detail?.statuses
        assertNotNull(statuses)
        assertEquals(4, statuses?.size)

        assertEquals("IN_TRANSIT", statuses?.get(0)?.code)
        assertEquals("Departed sorting facility", statuses?.get(0)?.label)
        assertEquals("Johannesburg Hub, Germiston", statuses?.get(0)?.location)
        assertEquals("2026-05-01T08:30:00Z", statuses?.get(0)?.time)


        statuses?.forEach { status ->
            assertNotNull(status.location)
        }
    }

    @Test
    fun `shipment DTO handles Fastway shipment data correctly`() {
        val json = """
        {
            "carrier": {
                "code": "fastway",
                "name": "Fastway Couriers"
            },
            "destination": {
                "city": "East London",
                "country": "ZA"
            },
            "estimatedDeliveryAt": "2026-05-02T17:30:00Z",
            "id": "shp_005",
            "lastStatus": {
                "code": "OUT_FOR_DELIVERY",
                "label": "Out for delivery"
            },
            "lastUpdatedAt": "2026-05-02T06:00:00Z",
            "origin": {
                "city": "Port Elizabeth",
                "country": "ZA"
            },
            "trackingNumber": "FW443322ZA"
        }
        """.trimIndent()

        val shipment = shipmentAdapter.fromJson(json)

        assertNotNull(shipment)
        assertEquals("shp_005", shipment?.id)
        assertEquals("fastway", shipment?.carrier?.code)
        assertEquals("Fastway Couriers", shipment?.carrier?.name)
        assertEquals("FW443322ZA", shipment?.trackingNumber)
        assertEquals("OUT_FOR_DELIVERY", shipment?.lastStatus?.code)
        assertEquals("Port Elizabeth", shipment?.origin?.city)
        assertEquals("East London", shipment?.destination?.city)
        assertEquals("ZA", shipment?.destination?.country)
    }

    @Test
    fun `shipment DTO handles DPD shipment with no delivery estimate`() {
        val json = """
        {
            "carrier": {
                "code": "dpd",
                "name": "DPD International"
            },
            "destination": {
                "city": "Stellenbosch",
                "country": "ZA"
            },
            "id": "shp_006",
            "lastStatus": {
                "code": "LABEL_CREATED",
                "label": "We have been notified of your parcel"
            },
            "lastUpdatedAt": "2026-05-02T10:00:00Z",
            "origin": {
                "city": "London",
                "country": "GB"
            },
            "trackingNumber": "DP556677UK"
        }
        """.trimIndent()

        val shipment = shipmentAdapter.fromJson(json)

        assertNotNull(shipment)
        assertEquals("shp_006", shipment?.id)
        assertNull(shipment?.estimatedDeliveryAt)
        assertEquals("GB", shipment?.origin?.country)
        assertEquals("ZA", shipment?.destination?.country)
    }
}