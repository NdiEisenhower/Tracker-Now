package com.eisen.trackernow.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShipmentsResponse(
    @param:Json(name = "shipments")
    val shipments: List<ShipmentDto>
)

@JsonClass(generateAdapter = true)
data class ShipmentDto(
    @param:Json(name = "id")
    val id: String? = null,

    @param:Json(name = "carrier")
    val carrier: CarrierDto,

    @param:Json(name = "trackingNumber")
    val trackingNumber: String,

    @param:Json(name = "lastStatus")
    val lastStatus: StatusDto,

    @param:Json(name = "lastUpdatedAt")
    val lastUpdatedAt: String,

    @param:Json(name = "origin")
    val origin: AddressDto,

    @param:Json(name = "destination")
    val destination: AddressDto,

    @param:Json(name = "estimatedDeliveryAt")
    val estimatedDeliveryAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ShipmentDetailResponse(
    @param:Json(name = "id")
    val id: String? = null,

    @param:Json(name = "carrier")
    val carrier: CarrierDto,

    @param:Json(name = "trackingNumber")
    val trackingNumber: String,

    @param:Json(name = "origin")
    val origin: AddressDto,

    @param:Json(name = "destination")
    val destination: AddressDto,

    @param:Json(name = "estimatedDeliveryAt")
    val estimatedDeliveryAt: String? = null,

    @param:Json(name = "statuses")
    val statuses: List<StatusTimelineDto>
)


@JsonClass(generateAdapter = true)
data class CarrierDto(
    @param:Json(name = "code")
    val code: String,

    @param:Json(name = "name")
    val name: String
)

@JsonClass(generateAdapter = true)
data class StatusDto(
    @param:Json(name = "code")
    val code: String,

    @param:Json(name = "label")
    val label: String
)

@JsonClass(generateAdapter = true)
data class AddressDto(
    @param:Json(name = "city")
    val city: String,

    @param:Json(name = "country")
    val country: String
)

@JsonClass(generateAdapter = true)
data class StatusTimelineDto(
    @param:Json(name = "time")
    val time: String,

    @param:Json(name = "code")
    val code: String,

    @param:Json(name = "label")
    val label: String,

    @param:Json(name = "location")
    val location: String? = null
)