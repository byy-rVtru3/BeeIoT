package com.app.mobile.data.api.models.queen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EggPhaseDto(
    @SerialName("standing") val standing: String,
    @SerialName("tilted") val tilted: String,
    @SerialName("lying") val lying: String
)
