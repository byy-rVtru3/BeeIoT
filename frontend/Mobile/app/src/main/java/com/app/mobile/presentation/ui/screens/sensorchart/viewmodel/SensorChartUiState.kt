package com.app.mobile.presentation.ui.screens.sensorchart.viewmodel

import com.app.mobile.domain.models.telemetry.SensorType
import com.app.mobile.domain.models.telemetry.TelemetryDataPoint
import java.time.LocalDate

sealed interface SensorChartUiState {
	data object Loading : SensorChartUiState
	data class Content(
		val sensorType: SensorType,
		val hubName: String,
		val currentValue: Double?,
		val dataPoints: List<TelemetryDataPoint>,
		val since: LocalDate
	) : SensorChartUiState

	data class Error(val message: String) : SensorChartUiState
}