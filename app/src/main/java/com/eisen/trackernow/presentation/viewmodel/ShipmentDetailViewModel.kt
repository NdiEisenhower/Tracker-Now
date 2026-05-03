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

    // Cache for processed detail to avoid reprocessing on config changes
    private var cachedDetail: ShipmentDetail? = null

    init {
        loadShipmentDetail()
    }

    fun loadShipmentDetail(forceRefresh: Boolean = false) {
        // If we have cached data and not forcing refresh, use it
        if (!forceRefresh && cachedDetail != null) {
            _shipmentDetail.value = Resource.Success(cachedDetail!!)
            return
        }

        viewModelScope.launch {
            _shipmentDetail.value = Resource.Loading
            try {
                val detail = getShipmentDetailUseCase(shipmentId)
                if (detail != null) {
                    // Process the detail to remove duplicates and sort timeline
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

    /**
     * Process shipment detail to:
     * 1. Remove duplicate timeline entries
     * 2. Sort timeline by timestamp (most recent first)
     * 3. Ensure unique entries based on time, location, and label
     */
    private fun processShipmentDetail(detail: ShipmentDetail): ShipmentDetail {
        // Create a sorted list of unique timeline entries
        val seen = mutableSetOf<String>()
        val uniqueTimeline = detail.timeline
            .sortedByDescending { it.time } // Sort by most recent first
            .filter { timelineItem ->
                val locationKey = timelineItem.location ?: ""
                val key = "${timelineItem.time}_${locationKey}_${timelineItem.label}"
                if (seen.contains(key)) {
                    false // Duplicate
                } else {
                    seen.add(key)
                    true // Unique
                }
            }

        // Return a new ShipmentDetail with the processed timeline
        return detail.copy(timeline = uniqueTimeline)
    }

    /**
     * Refresh the shipment detail from network
     */
    fun refresh() {
        loadShipmentDetail(forceRefresh = true)
    }
}

// Extension function to parse timestamp to milliseconds
private fun String.parseTimestamp(): Long {
    return try {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
        format.timeZone = java.util.TimeZone.getTimeZone("UTC")
        format.parse(this)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}