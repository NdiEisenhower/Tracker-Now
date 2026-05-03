package com.eisen.trackernow.domain.usecase

import com.eisen.trackernow.domain.repository.ShipmentRepository
import jakarta.inject.Inject

class RefreshShipmentsUseCase @Inject constructor(
    private val repository: ShipmentRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.refreshShipments()
    }
}