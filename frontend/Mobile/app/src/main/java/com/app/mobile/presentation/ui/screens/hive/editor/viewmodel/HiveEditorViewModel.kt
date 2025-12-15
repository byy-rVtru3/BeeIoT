package com.app.mobile.presentation.ui.screens.hive.editor.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class HiveEditorViewModel(
    private val getHiveUseCase: GetHiveUseCase,
    private val getQueensUseCase: GetQueensUseCase,
    private val getHubsUseCase: GetHubsUseCase,
    private val createHiveUseCase: CreateHiveUseCase,
    private val saveHiveUseCase: SaveHiveUseCase,
    private val addHiveToQueenUseCase: AddHiveToQueenUseCase,
    private val addHiveToHubUseCase: AddHiveToHubUseCase
) : ViewModel() {

    private val _hiveEditorUiState = MutableLiveData<HiveEditorUiState>(HiveEditorUiState.Loading)
    val hiveEditorUiState: LiveData<HiveEditorUiState> = _hiveEditorUiState

    private val _navigationEvent = MutableLiveData<HiveEditorNavigationEvent?>()
    val navigationEvent: LiveData<HiveEditorNavigationEvent?> = _navigationEvent

    val handler = CoroutineExceptionHandler { _, exception ->
        _hiveEditorUiState.value = HiveEditorUiState.Error(exception.message ?: "Unknown error")
        Log.e("HivesEditorViewModel", exception.message.toString())
    }

    fun loadHive(hiveId: String?) {
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
            _navigationEvent.value =
                HiveEditorNavigationEvent.NavigateToCreateQueen
        }
    }

    fun onCreateHubClick() {
        val currentState = _hiveEditorUiState.value
        if (currentState is HiveEditorUiState.Content) {
            _navigationEvent.value =
                HiveEditorNavigationEvent.NavigateToCreateHub
        }
    }

    fun onSaveClick() {
        val currentState = _hiveEditorUiState.value
        if (currentState is HiveEditorUiState.Content) {
            viewModelScope.launch(handler) {
                _hiveEditorUiState.value = HiveEditorUiState.Loading
                saveHiveUseCase(currentState.hiveEditorModel.toDomain())
                _navigationEvent.value = HiveEditorNavigationEvent.NavigateBack
            }
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}