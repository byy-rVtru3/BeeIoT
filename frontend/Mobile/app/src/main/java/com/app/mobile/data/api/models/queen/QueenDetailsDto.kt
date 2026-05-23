package com.app.mobile.data.api.models.queen

import kotlinx.serialization.Serializable

@Serializable
data class QueenDetailsDto(
    val name: String,
    val calendar: QueenCalendarDto
)
