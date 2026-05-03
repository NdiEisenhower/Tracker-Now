package com.eisen.trackernow.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateDataDto(
    val type: String = "",
    val shipmentId: String = "",
    val updatedAt: String = "",
    val timestamp: Long = 0,
    val changes: UpdateChangesDto = UpdateChangesDto()
)

@JsonClass(generateAdapter = true)
data class UpdateChangesDto(
    val newStatus: StatusDto = StatusDto("", ""),
    val location: String = "",
    val notes: String = ""
)