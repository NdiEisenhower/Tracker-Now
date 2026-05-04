package com.eisen.trackernow.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eisen.trackernow.domain.model.ShipmentDetail
import com.eisen.trackernow.domain.usecase.GetShipmentDetailUseCase
import com.eisen.trackernow.presentation.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ShipmentDetailViewModel @Inject constructor(
    private val getShipmentDetailUseCase: GetShipmentDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _shipmentDetail = MutableStateFlow<Resource<ShipmentDetail>>(Resource.Loading)
    val shipmentDetail: StateFlow<Resource<ShipmentDetail>> = _shipmentDetail.asStateFlow()

    private val shipmentId: String = savedStateHandle["shipmentId"] ?: ""

    private var cachedDetail: ShipmentDetail? = null

    init {
        loadShipmentDetail()
    }

    fun loadShipmentDetail(forceRefresh: Boolean = false) {
        if (!forceRefresh && cachedDetail != null) {
            _shipmentDetail.value = Resource.Success(cachedDetail!!)
            return
        }

        viewModelScope.launch {
            _shipmentDetail.value = Resource.Loading
            try {
                val detail = getShipmentDetailUseCase(shipmentId)
                if (detail != null) {
                    val processedDetail = processShipmentDetail(detail)
                    cachedDetail = processedDetail
                    _shipmentDetail.value = Resource.Success(processedDetail)
                } else {
                    _shipmentDetail.value = Resource.Error("Failed to load shipment details")
                }
            } catch (e: Exception) {
                _shipmentDetail.value = Resource.Error("Error: ${e.message}", e)
            }
        }
    }

    fun retry() {
        loadShipmentDetail(forceRefresh = true)
    }

    fun processShipmentDetail(detail: ShipmentDetail): ShipmentDetail {
        val seen = mutableSetOf<String>()
        val uniqueTimeline = detail.timeline
            .sortedByDescending { it.time }
            .filter { timelineItem ->
                val locationKey = timelineItem.location ?: ""
                val key = "${timelineItem.time}_${locationKey}_${timelineItem.label}"
                if (seen.contains(key)) {
                    false
                } else {
                    seen.add(key)
                    true
                }
            }

        return detail.copy(timeline = uniqueTimeline)
    }

}
