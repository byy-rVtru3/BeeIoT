package com.app.mobile.presentation.ui.screens.sensorchart.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.telemetry.SensorType
import com.app.mobile.domain.usecase.telemetry.AddWeightRecordUseCase
import com.app.mobile.domain.usecase.telemetry.GetTelemetryHistoryUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.sensorchart.SensorChartRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class SensorChartViewModel(
	savedStateHandle: SavedStateHandle,
	private val getTelemetryHistoryUseCase: GetTelemetryHistoryUseCase,
	private val addWeightRecordUseCase: AddWeightRecordUseCase
) : BaseViewModel<SensorChartUiState, SensorChartEvent>(SensorChartUiState.Loading) {

	private val route = savedStateHandle.toRoute<SensorChartRoute>()
	private val hubId = route.hubId
	val sensorType = SensorType.fromString(route.sensorType)
	private val hubName = route.hubName
	private val currentValue = route.currentValue

	private var since: LocalDate = LocalDate.now().withDayOfMonth(1)

	private val _isSubmittingWeight = MutableStateFlow(false)
	val isSubmittingWeight: StateFlow<Boolean> = _isSubmittingWeight

	init {
		loadData()
	}

	override fun handleError(exception: Throwable) {
		updateState { SensorChartUiState.Error(exception.message ?: "Unknown error") }
		Log.e("SensorChartViewModel", exception.message.toString())
	}

	fun loadData() {
		updateState { SensorChartUiState.Loading }
		launch {
			val sinceEpoch = since.atStartOfDay(ZoneOffset.UTC).toEpochSecond()
			when (val result = getTelemetryHistoryUseCase(hubId, sensorType, sinceEpoch)) {
				is ApiResult.Success -> {
					updateState {
						SensorChartUiState.Content(
							sensorType = sensorType,
							hubName = hubName,
							currentValue = currentValue,
							dataPoints = result.data,
							since = since
						)
					}
				}

				else -> {
					sendEvent(SensorChartEvent.ShowSnackBar(result.toErrorMessage()))
					updateState { SensorChartUiState.Error(result.toErrorMessage()) }
				}
			}
		}
	}

	fun updateSince(date: LocalDate) {
		since = date
		loadData()
	}

	fun submitWeightRecord(weight: Double, dateTime: LocalDateTime) {
		launch {
			_isSubmittingWeight.value = true
			try {
				val isoTime = dateTime
					.atZone(ZoneOffset.UTC)
					.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
				when (val result = addWeightRecordUseCase(hubId, weight, isoTime)) {
					is ApiResult.Success -> {
						sendEvent(SensorChartEvent.WeightAddedSuccessfully)
						loadData()
					}
					else -> sendEvent(SensorChartEvent.ShowSnackBar(result.toErrorMessage()))
				}
			} finally {
				_isSubmittingWeight.value = false
			}
		}
	}

	fun resetError() = loadData()
}
