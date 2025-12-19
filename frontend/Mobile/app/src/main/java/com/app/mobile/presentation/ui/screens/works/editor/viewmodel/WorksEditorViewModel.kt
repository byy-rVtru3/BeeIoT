package com.app.mobile.presentation.ui.screens.works.editor.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.hives.works.CreateWorkUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorkUseCase
import com.app.mobile.domain.usecase.hives.works.SaveWorkUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class WorksEditorViewModel(
    private val createWorkUseCase: CreateWorkUseCase,
    private val getWorkUseCase: GetWorkUseCase,
    private val saveWorkUseCase: SaveWorkUseCase
) : ViewModel() {

    private val _worksEditorUiState =
        MutableLiveData<WorksEditorUiState>(WorksEditorUiState.Loading)
    val worksEditorUiState: LiveData<WorksEditorUiState> = _worksEditorUiState

    private val _navigationEvent = MutableLiveData<WorksEditorNavigationEvent?>()
    val navigationEvent: LiveData<WorksEditorNavigationEvent?> = _navigationEvent

    private val handler = CoroutineExceptionHandler { _, exception ->
        _worksEditorUiState.value = WorksEditorUiState.Error(exception.message ?: "Unknown error")
        Log.e("WorksEditorViewModel", exception.message.toString())
    }


    fun loadWork(workId: String?, hiveId: String) {
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
                _navigationEvent.value =
                    WorksEditorNavigationEvent.NavigateToWorksList(currentState.work.hiveId)
            }
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}