package com.app.mobile.presentation.ui.screens.hub.editor.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.mappers.toEditorModel
import com.app.mobile.domain.usecase.hives.hub.GetHubByIdUseCase
import com.app.mobile.domain.usecase.hives.hub.SaveHubUseCase
import com.app.mobile.presentation.models.hub.HubModel
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.hub.editor.HubEditorRoute

class HubEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val getHubByIdUseCase: GetHubByIdUseCase,
    private val saveHubUseCase: SaveHubUseCase
) : BaseViewModel<HubEditorUiState, HubEditorEvent>(HubEditorUiState.Loading) {

    private val hubId = savedStateHandle.toRoute<HubEditorRoute>().hubId

    val isNew = hubId == null

    override fun handleError(exception: Throwable) {
        updateState { HubEditorUiState.Error(exception.message ?: "Unknown error") }
        Log.e("HubEditorViewModel", exception.message.toString())
    }

    fun loadHub() {
        updateState { HubEditorUiState.Loading }
        launch {
            if (hubId != null) {
                when (val result = getHubByIdUseCase(hubId)) {
                    is ApiResult.Success -> {
                        updateState { HubEditorUiState.Content(result.data.toEditorModel()) }
                    }
                    else -> {
                        updateState { HubEditorUiState.Error(result.toErrorMessage()) }
                    }
                }
            } else {
                updateState { HubEditorUiState.Content(HubModel(id = "", name = "")) }
            }
        }
    }

    fun onNameChange(name: String) {
        updateState { state ->
            if (state is HubEditorUiState.Content) {
                state.copy(hubModel = state.hubModel.copy(name = name))
            } else state
        }
    }

    fun onIdChange(id: String) {
        updateState { state ->
            if (state is HubEditorUiState.Content) {
                state.copy(hubModel = state.hubModel.copy(id = id))
            } else state
        }
    }

    fun onSaveClick() {
        val state = currentState
        if (state is HubEditorUiState.Content) {
            launch {
                when (val result = saveHubUseCase(state.hubModel.name, state.hubModel.id, isNew)) {
                    is ApiResult.Success -> sendEvent(HubEditorEvent.NavigateBack)
                    else -> sendEvent(HubEditorEvent.ShowSnackBar(result.toErrorMessage()))
                }
            }
        }
    }
}
