package com.eisen.trackernow.data

import com.eisen.trackernow.data.remote.dto.StatusDto

data class PushUpdate(
    val type: String = "",
    val shipmentId: String = "",
    val updatedAt: String = "",
    val timestamp: Long = 0,
    val changes: UpdateChanges = UpdateChanges()
)

data class UpdateChanges(
    val newStatus: StatusDto = StatusDto("", ""),
    val location: String = "",
    val notes: String = ""
)