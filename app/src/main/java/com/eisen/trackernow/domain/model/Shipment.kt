package com.eisen.trackernow.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Shipment(
    val id: String,
    val carrier: Carrier,
    val trackingNumber: String,
    val lastStatus: Status,
    val lastUpdatedAt: String,
    val origin: Address,
    val destination: Address,
    val estimatedDeliveryAt: String? = null,
    val isFavorite: Boolean = false
) : Parcelable

@Parcelize
data class Carrier(
    val code: String,
    val name: String
) : Parcelable

@Parcelize
data class Status(
    val code: String,
    val label: String
) : Parcelable{
    constructor() : this("", "")
}

@Parcelize
data class Address(
    val city: String,
    val country: String
) : Parcelable

@Parcelize
data class ShipmentDetail(
    val shipment: Shipment,
    val timeline: List<StatusTimeline>
) : Parcelable

@Parcelize
data class StatusTimeline(
    val time: String,
    val code: String,
    val label: String,
    val location: String? = null
) : Parcelable