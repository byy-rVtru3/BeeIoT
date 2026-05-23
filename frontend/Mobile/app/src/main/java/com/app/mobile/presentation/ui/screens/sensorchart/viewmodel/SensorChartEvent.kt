package com.app.mobile.presentation.ui.screens.sensorchart.viewmodel

sealed interface SensorChartEvent {
	data class ShowSnackBar(val message: String) : SensorChartEvent
	data object WeightAddedSuccessfully : SensorChartEvent
}
