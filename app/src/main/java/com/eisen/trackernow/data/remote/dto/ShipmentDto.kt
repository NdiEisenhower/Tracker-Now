package com.eisen.trackernow.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ShipmentsResponse(
    @Json(name = "shipments")
    val shipments: List<ShipmentDto>
)

@JsonClass(generateAdapter = true)
data class ShipmentDto(
    @Json(name = "id")
    val id: String? = null,

    @Json(name = "carrier")
    val carrier: CarrierDto,

    @Json(name = "trackingNumber")
    val trackingNumber: String,

    @Json(name = "lastStatus")
    val lastStatus: StatusDto,

    @Json(name = "lastUpdatedAt")
    val lastUpdatedAt: String,

    @Json(name = "origin")
    val origin: AddressDto,

    @Json(name = "destination")
    val destination: AddressDto,

    @Json(name = "estimatedDeliveryAt")
    val estimatedDeliveryAt: String? = null
)

@JsonClass(generateAdapter = true)
data class ShipmentDetailResponse(
    @Json(name = "id")
    val id: String? = null,

    @Json(name = "carrier")
    val carrier: CarrierDto,

    @Json(name = "trackingNumber")
    val trackingNumber: String,

    @Json(name = "origin")
    val origin: AddressDto,

    @Json(name = "destination")
    val destination: AddressDto,

    @Json(name = "estimatedDeliveryAt")
    val estimatedDeliveryAt: String? = null,

    @Json(name = "statuses")
    val statuses: List<StatusTimelineDto>
)


@JsonClass(generateAdapter = true)
data class CarrierDto(
    @Json(name = "code")
    val code: String,

    @Json(name = "name")
    val name: String
)

@JsonClass(generateAdapter = true)
data class StatusDto(
    @Json(name = "code")
    val code: String,

    @Json(name = "label")
    val label: String
)

@JsonClass(generateAdapter = true)
data class AddressDto(
    @Json(name = "city")
    val city: String,

    @Json(name = "country")
    val country: String
)

@JsonClass(generateAdapter = true)
data class StatusTimelineDto(
    @Json(name = "time")
    val time: String,

    @Json(name = "code")
    val code: String,

    @Json(name = "label")
    val label: String,

    @Json(name = "location")
    val location: String? = null
)