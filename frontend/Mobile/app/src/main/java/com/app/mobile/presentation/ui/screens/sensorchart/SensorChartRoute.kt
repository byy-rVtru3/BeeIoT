package com.app.mobile.presentation.ui.screens.sensorchart

import kotlinx.serialization.Serializable

@Serializable
data class SensorChartRoute(
	val hubId: String,
	val sensorType: String,
	val hubName: String,
	val currentValue: Double? = null
)