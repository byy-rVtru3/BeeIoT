package com.app.mobile.data.api.models.telemetry

import kotlinx.serialization.Serializable

@Serializable
data class TelemetryHistoryResponse(
	val status: String,
	val message: String,
	val data: List<TelemetryDataPointDto>? = null
)
