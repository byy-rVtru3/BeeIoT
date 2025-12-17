package com.app.mobile.data.api.models.queen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QueenCalendarResponse(
    @SerialName("status") val status: String,
    @SerialName("message") val message: String,
    @SerialName("data") val data: QueenCalendarDto
)
