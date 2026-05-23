package com.app.mobile.presentation.models.hub

data class HubDetailUi(
    val id: String,
    val name: String,
    val sensorReadings: SensorReadingsUi? = null
)
