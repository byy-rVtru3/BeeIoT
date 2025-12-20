package com.app.mobile.presentation.ui.screens.works.editor.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.hives.works.CreateWorkUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorkUseCase
import com.app.mobile.domain.usecase.hives.works.SaveWorkUseCase
import com.app.mobile.presentation.ui.screens.works.editor.WorkEditorRoute
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class WorksEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val createWorkUseCase: CreateWorkUseCase,
    private val getWorkUseCase: GetWorkUseCase,
    private val saveWorkUseCase: SaveWorkUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<WorkEditorRoute>()
    private val hiveId = route.hiveId
    private val workId = route.workId

    private val _worksEditorUiState =
        MutableStateFlow<WorksEditorUiState>(WorksEditorUiState.Loading)
    val worksEditorUiState = _worksEditorUiState.asStateFlow()

    private val _navigationEvent = Channel<WorksEditorNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val handler = CoroutineExceptionHandler { _, exception ->
        _worksEditorUiState.value = WorksEditorUiState.Error(exception.message ?: "Unknown error")
        Log.e("WorksEditorViewModel", exception.message.toString())
    }


    fun loadWork() {
        _worksEditorUiState.value = WorksEditorUiState.Loading
        viewModelScope.launch(handler) {

            val deferredWork = async { if (workId != null) getWorkUseCase(workId) else null }

            val foundWork = deferredWork.await()

            val work = foundWork ?: createWorkUseCase(hiveId)

            _worksEditorUiState.value = WorksEditorUiState.Content(work.toUiModel())
        }
    }

    fun onTitleChange(title: String) {
        val currentState = _worksEditorUiState.value
        if (currentState is WorksEditorUiState.Content) {
            val updatedWork = currentState.work.copy(title = title)
            _worksEditorUiState.value = WorksEditorUiState.Content(updatedWork)
        }
    }

    fun onTextChange(text: String) {
        val currentState = _worksEditorUiState.value
        if (currentState is WorksEditorUiState.Content) {
            val updatedWork = currentState.work.copy(text = text)
            _worksEditorUiState.value = WorksEditorUiState.Content(updatedWork)
        }
    }

    fun onSaveClick() {
        val currentState = _worksEditorUiState.value
        if (currentState is WorksEditorUiState.Content) {
            viewModelScope.launch(handler) {
                saveWorkUseCase(currentState.work.toDomain())
                _navigationEvent.send(
                    WorksEditorNavigationEvent.NavigateToWorksList(currentState.work.hiveId))
            }
        }
    }
}