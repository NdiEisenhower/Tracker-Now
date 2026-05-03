package com.eisen.trackernow.domain.usecase

import com.eisen.trackernow.domain.model.Shipment
import com.eisen.trackernow.domain.repository.ShipmentRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class FilterShipmentsUseCase @Inject constructor() {
    operator fun invoke(shipments: List<Shipment>, statusCode: String): List<Shipment> {
        return shipments.filter { it.lastStatus.code == statusCode }
    }
}