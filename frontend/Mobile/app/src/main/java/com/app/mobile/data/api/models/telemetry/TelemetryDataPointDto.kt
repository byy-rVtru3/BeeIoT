package com.app.mobile.data.api.models.telemetry

import kotlinx.serialization.Serializable

@Serializable
data class TelemetryDataPointDto(
	val time: Long,
	val value: Double
)
