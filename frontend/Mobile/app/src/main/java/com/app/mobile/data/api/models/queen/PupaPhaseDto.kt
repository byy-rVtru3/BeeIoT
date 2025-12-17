package com.app.mobile.data.api.models.queen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PupaPhaseDto(
    @SerialName("start") val start: String,
    @SerialName("end") val end: String,
    @SerialName("selection") val selection: String
)
