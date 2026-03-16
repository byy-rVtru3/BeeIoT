package com.app.mobile.presentation.ui.screens.hub.details.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.domain.mappers.toDetailUi
import com.app.mobile.domain.usecase.hives.hub.GetHubByIdUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.hub.details.HubRoute

class HubViewModel(
    savedStateHandle: SavedStateHandle,
    private val getHubByIdUseCase: GetHubByIdUseCase
) : BaseViewModel<HubUiState, HubEvent>(HubUiState.Loading) {

    private val hubId = savedStateHandle.toRoute<HubRoute>().hubId

    override fun handleError(exception: Throwable) {
        updateState { HubUiState.Error(exception.message ?: "Unknown error") }
        Log.e("HubViewModel", exception.message.toString())
    }

    fun loadHub() {
        updateState { HubUiState.Loading }
        launch {
            val hub = getHubByIdUseCase(hubId)
            if (hub == null) {
                sendEvent(HubEvent.ShowSnackBar("Хаб не найден"))
            } else {
                updateState { HubUiState.Content(hub.toDetailUi()) }
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

    fun onHubListClick() {
        sendEvent(HubEvent.NavigateToHubList)
    }

    fun onNotificationsClick() {
        if (currentState is HubUiState.Content) {
            sendEvent(HubEvent.NavigateToNotificationByHub(hubId))
        }
    }
}
