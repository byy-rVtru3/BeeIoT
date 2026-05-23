package com.app.mobile.presentation.ui.screens.hub.details.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.hives.hub.DeleteHubUseCase
import com.app.mobile.domain.usecase.hives.hub.GetHubWithSensorsUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.hub.details.HubRoute

class HubViewModel(
    savedStateHandle: SavedStateHandle,
    private val getHubWithSensorsUseCase: GetHubWithSensorsUseCase,
    private val deleteHubUseCase: DeleteHubUseCase,
) : BaseViewModel<HubUiState, HubEvent>(HubUiState.Loading) {

    private val hubId = savedStateHandle.toRoute<HubRoute>().hubId

    override fun handleError(exception: Throwable) {
        updateState { HubUiState.Error(exception.message ?: "Unknown error") }
        Log.e("HubViewModel", exception.message.toString())
    }

    fun loadHub() {
        updateState { HubUiState.Loading }
        launch {
            when (val result = getHubWithSensorsUseCase(hubId)) {
                is ApiResult.Success -> {
                    updateState { HubUiState.Content(result.data.toUiModel()) }
                }
                else -> {
                    sendEvent(HubEvent.ShowSnackBar(result.toErrorMessage()))
                }
            }
        }
    }

    fun refresh() {
        val current = currentState as? HubUiState.Content ?: return
        updateState { current.copy(isRefreshing = true) }
        launch {
            when (val result = getHubWithSensorsUseCase(hubId)) {
                is ApiResult.Success -> {
                    updateState { HubUiState.Content(result.data.toUiModel()) }
                }
                else -> {
                    updateState { current.copy(isRefreshing = false) }
                    sendEvent(HubEvent.ShowSnackBar(result.toErrorMessage()))
                }
            }
        }
    }

    fun resetError() {
        loadHub()
    }

    fun onEditClick() {
        if (currentState is HubUiState.Content) {
            sendEvent(HubEvent.NavigateToHubEdit(hubId))
        }
    }

    fun onDeleteClick() {
        launch {
            when (val result = deleteHubUseCase(hubId)) {
                is ApiResult.Success -> sendEvent(HubEvent.NavigateToHubList)
                else -> sendEvent(HubEvent.ShowSnackBar(result.toErrorMessage()))
            }
        }
    }

    fun onHubListClick() {
        sendEvent(HubEvent.NavigateToHubList)
    }

    fun onTemperatureClick() {
        val state = currentState as? HubUiState.Content ?: return
        sendEvent(HubEvent.NavigateToSensorChart(
            hubId = hubId,
            sensorType = "temperature",
            hubName = state.hub.name,
            currentValue = state.hub.sensorReadings?.temperatureSensor?.temperature
        ))
    }

    fun onNoiseClick() {
        val state = currentState as? HubUiState.Content ?: return
        sendEvent(HubEvent.NavigateToSensorChart(
            hubId = hubId,
            sensorType = "noise",
            hubName = state.hub.name,
            currentValue = state.hub.sensorReadings?.noiseSensor?.noise
        ))
    }

    fun onWeightClick() {
        val state = currentState as? HubUiState.Content ?: return
        sendEvent(HubEvent.NavigateToSensorChart(
            hubId = hubId,
            sensorType = "weight",
            hubName = state.hub.name,
            currentValue = state.hub.sensorReadings?.weightSensor?.weight
        ))
    }
}
