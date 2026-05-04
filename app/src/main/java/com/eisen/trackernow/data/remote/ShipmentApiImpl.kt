package com.eisen.trackernow.data.remote

import com.eisen.trackernow.data.remote.dto.ShipmentDetailResponse
import com.eisen.trackernow.data.remote.dto.ShipmentDto
import jakarta.inject.Inject

class ShipmentApiImpl @Inject constructor(
    private val retrofit: retrofit2.Retrofit
) : ShipmentApi {

    private val api: ShipmentApi = retrofit.create(ShipmentApi::class.java)

    override suspend fun getShipments(): Map<String, ShipmentDto> {
        return api.getShipments()
    }

    override suspend fun getShipmentDetail(id: String): ShipmentDetailResponse {
        return api.getShipmentDetail(id)
    }
}