package com.eisen.trackernow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shipments")
data class ShipmentEntity(
    @PrimaryKey
    val id: String,
    val carrierCode: String,
    val carrierName: String,
    val trackingNumber: String,
    val lastStatusCode: String,
    val lastStatusLabel: String,
    val lastUpdatedAt: String,
    val originCity: String,
    val originCountry: String,
    val destinationCity: String,
    val destinationCountry: String,
    val estimatedDeliveryAt: String?,
    val isFavorite: Boolean = false,
    val lastCacheTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "status_timeline")
data class StatusTimelineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val shipmentId: String,
    val time: String,
    val code: String,
    val label: String,
    val location: String?
)