package com.eisen.trackernow.domain.usecase

import com.eisen.trackernow.domain.model.Shipment
import jakarta.inject.Inject

class FilterShipmentsUseCase @Inject constructor() {
    operator fun invoke(shipments: List<Shipment>, statusCode: String): List<Shipment> {
        return shipments.filter { it.lastStatus.code == statusCode }
    }
}