package com.app.mobile.presentation.ui.screens.hive.editor.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.app.mobile.presentation.ui.screens.hive.editor.HiveEditorRoute
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HiveEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val getHiveUseCase: GetHiveUseCase,
    private val getQueensUseCase: GetQueensUseCase,
    private val getHubsUseCase: GetHubsUseCase,
    private val createHiveUseCase: CreateHiveUseCase,
    private val saveHiveUseCase: SaveHiveUseCase,
    private val addHiveToQueenUseCase: AddHiveToQueenUseCase,
    private val addHiveToHubUseCase: AddHiveToHubUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<HiveEditorRoute>()
    private val hiveId = route.hiveId

    private val _hiveEditorUiState = MutableStateFlow<HiveEditorUiState>(HiveEditorUiState.Loading)
    val hiveEditorUiState = _hiveEditorUiState.asStateFlow()

    private val _navigationEvent = Channel<HiveEditorNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    val handler = CoroutineExceptionHandler { _, exception ->
        _hiveEditorUiState.value = HiveEditorUiState.Error(exception.message ?: "Unknown error")
        Log.e("HivesEditorViewModel", exception.message.toString())
    }

    fun loadHive() {
        _hiveEditorUiState.value = HiveEditorUiState.Loading
        viewModelScope.launch(handler) {
            coroutineScope {
                val deferredQueens = async { getQueensUseCase() }
                val deferredHubs = async { getHubsUseCase() }

                val deferredHive = async { if (hiveId != null) getHiveUseCase(hiveId) else null }

                val queens = deferredQueens.await()
                val hubs = deferredHubs.await()
                val hive = deferredHive.await()

                val uiModel =
                    hive?.toEditor(queens, hubs) ?: createHiveUseCase().toPresentation(queens, hubs)

                _hiveEditorUiState.value = HiveEditorUiState.Content(uiModel)
            }
        }
    }

    fun onNameChange(name: String) {
        val currentState = _hiveEditorUiState.value
        if (currentState is HiveEditorUiState.Content) {
            val updatedHive = currentState.hiveEditorModel.copy(name = name)
            _hiveEditorUiState.value = HiveEditorUiState.Content(updatedHive)
        }
    }

    fun onHubAdd(hubId: String) {
        val currentState = _hiveEditorUiState.value
        if (currentState is HiveEditorUiState.Content) {
            val updatedHive = currentState.hiveEditorModel.copy(connectedHubId = hubId)
            viewModelScope.launch(handler) {

                addHiveToHubUseCase(hubId, currentState.hiveEditorModel.id)

                _hiveEditorUiState.value = HiveEditorUiState.Content(updatedHive)
            }
        }
    }

    fun onQueenAdd(queenId: String) {
        val currentState = _hiveEditorUiState.value
        if (currentState is HiveEditorUiState.Content) {
            val updatedHive = currentState.hiveEditorModel.copy(connectedQueenId = queenId)
            viewModelScope.launch(handler) {

                addHiveToQueenUseCase(queenId, currentState.hiveEditorModel.id)

                _hiveEditorUiState.value = HiveEditorUiState.Content(updatedHive)
            }
        }
    }

    fun onCreateQueenClick() {
        val currentState = _hiveEditorUiState.value
        if (currentState is HiveEditorUiState.Content) {
            viewModelScope.launch(handler) {
                _navigationEvent.send(
                    HiveEditorNavigationEvent.NavigateToCreateQueen
                )
            }
        }
    }

    fun onCreateHubClick() {
        val currentState = _hiveEditorUiState.value
        if (currentState is HiveEditorUiState.Content) {
            viewModelScope.launch(handler) {
                _navigationEvent.send(
                    HiveEditorNavigationEvent.NavigateToCreateHub
                )
            }
        }
    }

    fun onSaveClick() {
        val currentState = _hiveEditorUiState.value
        if (currentState is HiveEditorUiState.Content) {
            viewModelScope.launch(handler) {
                _hiveEditorUiState.value = HiveEditorUiState.Loading
                saveHiveUseCase(currentState.hiveEditorModel.toDomain())
                _navigationEvent.send(HiveEditorNavigationEvent.NavigateBack)
            }
        }
    }
}