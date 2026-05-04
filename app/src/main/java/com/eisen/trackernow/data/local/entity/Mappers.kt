package com.eisen.trackernow.data.local.entity

import com.eisen.trackernow.data.remote.dto.ShipmentDetailResponse
import com.eisen.trackernow.data.remote.dto.ShipmentDto
import com.eisen.trackernow.data.remote.dto.StatusTimelineDto
import com.eisen.trackernow.domain.model.Address
import com.eisen.trackernow.domain.model.Carrier
import com.eisen.trackernow.domain.model.Shipment
import com.eisen.trackernow.domain.model.Status
import com.eisen.trackernow.domain.model.StatusTimeline

fun ShipmentDto.toEntity(): ShipmentEntity {
    return ShipmentEntity(
        id = this.id ?: "",
        carrierCode = this.carrier.code,
        carrierName = this.carrier.name,
        trackingNumber = this.trackingNumber,
        lastStatusCode = this.lastStatus.code,
        lastStatusLabel = this.lastStatus.label,
        lastUpdatedAt = this.lastUpdatedAt,
        originCity = this.origin.city,
        originCountry = this.origin.country,
        destinationCity = this.destination.city,
        destinationCountry = this.destination.country,
        estimatedDeliveryAt = this.estimatedDeliveryAt,
        isFavorite = false

    )
}

fun ShipmentDetailResponse.toEntity(id: String): ShipmentEntity {
    return ShipmentEntity(
        id = id,
        carrierCode = this.carrier.code,
        carrierName = this.carrier.name,
        trackingNumber = this.trackingNumber,
        lastStatusCode = this.statuses.lastOrNull()?.code ?: "",
        lastStatusLabel = this.statuses.lastOrNull()?.label ?: "",
        lastUpdatedAt = this.statuses.lastOrNull()?.time ?: "",
        originCity = this.origin.city,
        originCountry = this.origin.country,
        destinationCity = this.destination.city,
        destinationCountry = this.destination.country,
        estimatedDeliveryAt = this.estimatedDeliveryAt,
        isFavorite = false
    )
}

fun StatusTimelineDto.toEntity(shipmentId: String): StatusTimelineEntity {
    return StatusTimelineEntity(
        shipmentId = shipmentId,
        time = time,
        code = code,
        label = label,
        location = location
    )
}

fun ShipmentEntity.toDomain(): Shipment {
    return Shipment(
        id = id,
        carrier = Carrier(carrierCode, carrierName),
        trackingNumber = trackingNumber,
        lastStatus = Status(lastStatusCode, lastStatusLabel),
        lastUpdatedAt = lastUpdatedAt,
        origin = Address(originCity, originCountry),
        destination = Address(destinationCity, destinationCountry),
        estimatedDeliveryAt = estimatedDeliveryAt,
        isFavorite = isFavorite
    )
}

fun StatusTimelineEntity.toDomain(): StatusTimeline {
    return StatusTimeline(
        time = time,
        code = code,
        label = label,
        location = location
    )
}

fun StatusTimelineDto.toDomain(): StatusTimeline {
    return StatusTimeline(
        time = time,
        code = code,
        label = label,
        location = location
    )
}