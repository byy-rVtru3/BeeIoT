package com.app.mobile.domain.models.telemetry

data class SensorReadings(
    val temperature: Double?,
    val temperatureTime: Long?,
    val noise: Double?,
    val noiseTime: Long?,
    val weight: Double?,
    val weightTime: Long?
)
