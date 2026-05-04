package com.eisen.trackernow.data.remote

import com.eisen.trackernow.data.remote.dto.ShipmentDetailResponse
import com.eisen.trackernow.data.remote.dto.ShipmentDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ShipmentApi {
    @GET("shipments.json")
    suspend fun getShipments(): Map<String, ShipmentDto>

    @GET("shipment_details/{id}.json")
    suspend fun getShipmentDetail(@Path("id") id: String): ShipmentDetailResponse
}