package com.app.mobile.data.api.models.telemetry

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LastSensorReadingDto(
    val temperature: Double,
    @SerialName("temperature_time") val temperatureTime: Long,
    val noise: Double,
    @SerialName("noise_time") val noiseTime: Long,
    val weight: Double,
    @SerialName("weight_time") val weightTime: Long
)
