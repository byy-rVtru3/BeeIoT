package com.app.mobile.data.api.models.telemetry

import kotlinx.serialization.Serializable

@Serializable
data class SetWeightRequest(
	val hub: String,
	val time: String,
	val weight: Double
)
