package com.eisen.trackernow.domain.usecase

import com.eisen.trackernow.domain.model.ShipmentDetail
import com.eisen.trackernow.domain.repository.ShipmentRepository
import jakarta.inject.Inject

class GetShipmentDetailUseCase @Inject constructor(
    private val repository: ShipmentRepository
) {
    suspend operator fun invoke(shipmentId: String): ShipmentDetail? {
        return repository.getShipmentDetail(shipmentId)
    }
}