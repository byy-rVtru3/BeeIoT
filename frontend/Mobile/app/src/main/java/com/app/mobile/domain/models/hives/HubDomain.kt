package com.app.mobile.domain.models.hives

import com.app.mobile.domain.models.telemetry.SensorReadings

data class HubDomain(
    val id: String,
    val name: String,
    val sensorReadings: SensorReadings? = null
)
