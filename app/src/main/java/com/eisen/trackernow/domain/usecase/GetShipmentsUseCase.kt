package com.eisen.trackernow.domain.usecase

import com.eisen.trackernow.domain.model.Shipment
import com.eisen.trackernow.domain.repository.ShipmentRepository
import com.eisen.trackernow.presentation.util.Resource
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetShipmentsUseCase @Inject constructor(
    private val repository: ShipmentRepository
) {
    operator fun invoke(): Flow<Resource<List<Shipment>>> {
        return repository.getShipmentsStream()
    }
}