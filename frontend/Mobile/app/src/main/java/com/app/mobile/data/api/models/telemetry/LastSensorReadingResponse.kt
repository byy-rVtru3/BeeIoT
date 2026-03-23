package com.app.mobile.data.api.models.telemetry

import kotlinx.serialization.Serializable

@Serializable
data class LastSensorReadingResponse(
    val status: String,
    val message: String,
    val data: LastSensorReadingDto? = null
)
