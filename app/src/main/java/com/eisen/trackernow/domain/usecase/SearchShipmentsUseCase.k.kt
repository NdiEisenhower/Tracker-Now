package com.eisen.trackernow.domain.usecase

import com.eisen.trackernow.domain.model.Shipment
import jakarta.inject.Inject

class SearchShipmentsUseCase @Inject constructor() {
    operator fun invoke(shipments: List<Shipment>, query: String): List<Shipment> {
        return shipments.filter { shipment ->
            shipment.trackingNumber.contains(query, ignoreCase = true) ||
                    shipment.carrier.name.contains(query, ignoreCase = true) ||
                    shipment.carrier.code.contains(query, ignoreCase = true)
        }
    }
}