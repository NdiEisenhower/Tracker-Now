package com.eisen.trackernow.domain.usecase

import com.eisen.trackernow.domain.model.Shipment
import com.eisen.trackernow.domain.repository.ShipmentRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class SearchShipmentsUseCase @Inject constructor() {
    operator fun invoke(shipments: List<Shipment>, query: String): List<Shipment> {
        return shipments.filter { shipment ->
            shipment.trackingNumber.contains(query, ignoreCase = true) ||
                    shipment.carrier.name.contains(query, ignoreCase = true) ||
                    shipment.carrier.code.contains(query, ignoreCase = true)
        }
    }
}