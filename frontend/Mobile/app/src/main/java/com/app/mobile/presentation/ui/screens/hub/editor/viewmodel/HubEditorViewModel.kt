package com.app.mobile.presentation.ui.screens.hub.editor.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toEditorModel
import com.app.mobile.domain.usecase.hives.hub.GetHubByIdUseCase
import com.app.mobile.domain.usecase.hives.hub.SaveHubUseCase
import com.app.mobile.presentation.models.hub.HubEditorModel
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.hub.editor.HubEditorRoute
import java.util.UUID

class HubEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val getHubByIdUseCase: GetHubByIdUseCase,
    private val saveHubUseCase: SaveHubUseCase
) : BaseViewModel<HubEditorUiState, HubEditorEvent>(HubEditorUiState.Loading) {

    private val hubId = savedStateHandle.toRoute<HubEditorRoute>().hubId

    override fun handleError(exception: Throwable) {
        updateState { HubEditorUiState.Error(exception.message ?: "Unknown error") }
        Log.e("HubEditorViewModel", exception.message.toString())
    }

    fun loadHub() {
        updateState { HubEditorUiState.Loading }
        launch {
            val model = if (hubId != null) {
                getHubByIdUseCase(hubId)?.toEditorModel()
                    ?: HubEditorModel(id = hubId, hiveId = null, name = "", ipAddress = "", port = "")
            } else {
                HubEditorModel(
                    id = UUID.randomUUID().toString(),
                    hiveId = null,
                    name = "",
                    ipAddress = "",
                    port = ""
                )
            }
            updateState { HubEditorUiState.Content(model) }
        }
    }

    fun onNameChange(name: String) {
        updateState { state ->
            if (state is HubEditorUiState.Content) {
                state.copy(hubEditorModel = state.hubEditorModel.copy(name = name))
            } else state
        }
    }

    fun onIpAddressChange(ipAddress: String) {
        updateState { state ->
            if (state is HubEditorUiState.Content) {
                state.copy(hubEditorModel = state.hubEditorModel.copy(ipAddress = ipAddress))
            } else state
        }
    }

    fun onPortChange(port: String) {
        updateState { state ->
            if (state is HubEditorUiState.Content) {
                state.copy(hubEditorModel = state.hubEditorModel.copy(port = port))
            } else state
        }
    }

    fun onSaveClick() {
        val state = currentState
        if (state is HubEditorUiState.Content) {
            launch {
                saveHubUseCase(state.hubEditorModel.toDomain())
                sendEvent(HubEditorEvent.NavigateBack)
            }
        }
    }
}
