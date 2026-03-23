package com.app.mobile.presentation.ui.screens.hive.editor.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toEditor
import com.app.mobile.domain.mappers.toPresentation
import com.app.mobile.domain.scenario.GetHiveScenario
import com.app.mobile.domain.usecase.hives.hive.CreateHiveUseCase
import com.app.mobile.domain.usecase.hives.hive.GetHivePreviewUseCase
import com.app.mobile.domain.usecase.hives.hive.GetHiveUseCase
import com.app.mobile.domain.usecase.hives.hive.SaveHiveUseCase
import com.app.mobile.domain.usecase.hives.hub.AddHiveToHubUseCase
import com.app.mobile.domain.usecase.hives.hub.GetHubsUseCase
import com.app.mobile.domain.usecase.hives.queen.AddHiveToQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueensUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.hive.editor.HiveEditorRoute
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class HiveEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val getHiveScenario: GetHiveScenario,
    private val getQueensUseCase: GetQueensUseCase,
    private val getHubsUseCase: GetHubsUseCase,
    private val createHiveUseCase: CreateHiveUseCase,
    private val saveHiveUseCase: SaveHiveUseCase,
    private val addHiveToQueenUseCase: AddHiveToQueenUseCase,
    private val addHiveToHubUseCase: AddHiveToHubUseCase
) : BaseViewModel<HiveEditorUiState, HiveEditorEvent>(HiveEditorUiState.Loading) {

    private val route = savedStateHandle.toRoute<HiveEditorRoute>()
    private val hiveName = route.hiveName
    private val isNew = hiveName == null

    override fun handleError(exception: Throwable) {
        updateState { HiveEditorUiState.Error(exception.message ?: "Unknown error") }
        Log.e("HivesEditorViewModel", exception.message.toString())
    }

    fun loadHive() {
        updateState { HiveEditorUiState.Loading }
        launch {
            coroutineScope {
                val deferredQueens = async { getQueensUseCase() }
                val deferredHubs = async { getHubsUseCase() }

                val queensResult = deferredQueens.await()
                val hubsResult = deferredHubs.await()

                val queens = when (queensResult) {
                    is ApiResult.Success -> queensResult.data
                    else -> emptyList()
                }
                val hubs = when (hubsResult) {
                    is ApiResult.Success -> hubsResult.data
                    else -> emptyList()
                }

                if (hiveName != null) {
                    when (val hiveResult = getHiveScenario(hiveName)) {
                        is ApiResult.Success -> {
                            val uiModel = hiveResult.data.toEditor(queens, hubs)
                            updateState { HiveEditorUiState.Content(uiModel) }
                        }

                        else -> {
                            updateState { HiveEditorUiState.Error(hiveResult.toErrorMessage()) }
                        }
                    }
                } else {
                    val uiModel = createHiveUseCase().toPresentation(queens, hubs)
                    updateState { HiveEditorUiState.Content(uiModel) }
                }
            }
        }
    }

    fun resetError() = loadHive()
    fun onNameChange(name: String) {
        val state = currentState
        if (state is HiveEditorUiState.Content) {
            val updatedHive = state.hiveEditorModel.copy(name = name)
            updateState { HiveEditorUiState.Content(updatedHive) }
        }
    }

    fun onHubAdd(hubId: String) {
        val state = currentState
        if (state is HiveEditorUiState.Content) {
            val updatedHive = state.hiveEditorModel.copy(connectedHubId = hubId)
            if (isNew) {
                updateState { HiveEditorUiState.Content(updatedHive) }
            } else {
                launch {
                    addHiveToHubUseCase(hiveName!!, hubId)
                    updateState { HiveEditorUiState.Content(updatedHive) }
                }
            }
        }
    }

    fun onQueenAdd(queenName: String) {
        val state = currentState
        if (state is HiveEditorUiState.Content) {
            val updatedHive = state.hiveEditorModel.copy(connectedQueenName = queenName)
            if (isNew) {
                updateState { HiveEditorUiState.Content(updatedHive) }
            } else {
                launch {
                    addHiveToQueenUseCase(hiveName!!, queenName)
                    updateState { HiveEditorUiState.Content(updatedHive) }
                }
            }
        }
    }

    fun onCreateQueenClick() {
        if (currentState is HiveEditorUiState.Content) {
            sendEvent(
                HiveEditorEvent.NavigateToCreateQueen
            )
        }
    }

    fun onCreateHubClick() {
        if (currentState is HiveEditorUiState.Content) {
            sendEvent(
                HiveEditorEvent.NavigateToCreateHub
            )
        }
    }

    fun onSaveClick() {
        val state = currentState
        if (state is HiveEditorUiState.Content) {
            launch {
                updateState { HiveEditorUiState.Loading }
                when (val result = saveHiveUseCase(hiveName, state.hiveEditorModel.name)) {
                    is ApiResult.Success -> {
                        if (isNew) {
                            val createdHiveName = state.hiveEditorModel.name
                            state.hiveEditorModel.connectedHubId?.let { hubId ->
                                addHiveToHubUseCase(createdHiveName, hubId)
                            }
                            state.hiveEditorModel.connectedQueenName?.let { queenName ->
                                addHiveToQueenUseCase(createdHiveName, queenName)
                            }
                        }
                        sendEvent(HiveEditorEvent.NavigateBack)
                    }
                    else -> {
                        sendEvent(HiveEditorEvent.ShowSnackBar(result.toErrorMessage()))
                        updateState { HiveEditorUiState.Content(state.hiveEditorModel) }
                    }
                }
            }
        }
    }
}
