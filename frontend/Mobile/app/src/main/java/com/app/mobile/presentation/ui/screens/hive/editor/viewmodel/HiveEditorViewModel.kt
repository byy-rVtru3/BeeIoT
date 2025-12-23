package com.app.mobile.presentation.ui.screens.hive.editor.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toEditor
import com.app.mobile.domain.mappers.toPresentation
import com.app.mobile.domain.usecase.hives.hive.CreateHiveUseCase
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
    private val getHiveUseCase: GetHiveUseCase,
    private val getQueensUseCase: GetQueensUseCase,
    private val getHubsUseCase: GetHubsUseCase,
    private val createHiveUseCase: CreateHiveUseCase,
    private val saveHiveUseCase: SaveHiveUseCase,
    private val addHiveToQueenUseCase: AddHiveToQueenUseCase,
    private val addHiveToHubUseCase: AddHiveToHubUseCase
) : BaseViewModel<HiveEditorUiState, HiveEditorNavigationEvent>(HiveEditorUiState.Loading) {

    private val route = savedStateHandle.toRoute<HiveEditorRoute>()
    private val hiveId = route.hiveId

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

                val deferredHive = async { if (hiveId != null) getHiveUseCase(hiveId) else null }

                val queens = deferredQueens.await()
                val hubs = deferredHubs.await()
                val hive = deferredHive.await()

                val uiModel =
                    hive?.toEditor(queens, hubs) ?: createHiveUseCase().toPresentation(queens, hubs)

                updateState { HiveEditorUiState.Content(uiModel) }
            }
        }
    }

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
            launch {

                addHiveToHubUseCase(hubId, state.hiveEditorModel.id)

                updateState { HiveEditorUiState.Content(updatedHive) }
            }
        }
    }

    fun onQueenAdd(queenId: String) {
        val state = currentState
        if (state is HiveEditorUiState.Content) {
            val updatedHive = state.hiveEditorModel.copy(connectedQueenId = queenId)
            launch {

                addHiveToQueenUseCase(queenId, state.hiveEditorModel.id)

                updateState { HiveEditorUiState.Content(updatedHive) }
            }
        }
    }

    fun onCreateQueenClick() {
        if (currentState is HiveEditorUiState.Content) {
            sendEvent(
                HiveEditorNavigationEvent.NavigateToCreateQueen
            )
        }
    }

    fun onCreateHubClick() {
        if (currentState is HiveEditorUiState.Content) {
            sendEvent(
                HiveEditorNavigationEvent.NavigateToCreateHub
            )
        }
    }

    fun onSaveClick() {
        val state = currentState
        if (state is HiveEditorUiState.Content) {
            launch {
                updateState { HiveEditorUiState.Loading }
                saveHiveUseCase(state.hiveEditorModel.toDomain())
                sendEvent(HiveEditorNavigationEvent.NavigateBack)
            }
        }
    }
}