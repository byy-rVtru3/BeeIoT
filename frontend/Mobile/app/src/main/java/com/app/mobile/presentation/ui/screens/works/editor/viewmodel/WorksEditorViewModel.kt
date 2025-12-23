package com.app.mobile.presentation.ui.screens.works.editor.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.hives.works.CreateWorkUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorkUseCase
import com.app.mobile.domain.usecase.hives.works.SaveWorkUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.works.editor.WorkEditorRoute
import kotlinx.coroutines.async

class WorksEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val createWorkUseCase: CreateWorkUseCase,
    private val getWorkUseCase: GetWorkUseCase,
    private val saveWorkUseCase: SaveWorkUseCase
) : BaseViewModel<WorksEditorUiState, WorksEditorNavigationEvent>(WorksEditorUiState.Loading) {

    private val route = savedStateHandle.toRoute<WorkEditorRoute>()
    private val hiveId = route.hiveId
    private val workId = route.workId

    override fun handleError(exception: Throwable) {
        updateState { WorksEditorUiState.Error(exception.message ?: "Unknown error") }
        Log.e("WorksEditorViewModel", exception.message.toString())
    }

    fun loadWork() {
        updateState { WorksEditorUiState.Loading }
        launch {

            val deferredWork = async { if (workId != null) getWorkUseCase(workId) else null }

            val foundWork = deferredWork.await()

            val work = foundWork ?: createWorkUseCase(hiveId)

            updateState { WorksEditorUiState.Content(work.toUiModel()) }
        }
    }

    fun onTitleChange(title: String) {
        val state = currentState
        if (state is WorksEditorUiState.Content) {
            val updatedWork = state.work.copy(title = title)
            updateState { WorksEditorUiState.Content(updatedWork) }
        }
    }

    fun onTextChange(text: String) {
        val state = currentState
        if (state is WorksEditorUiState.Content) {
            val updatedWork = state.work.copy(text = text)
            updateState { WorksEditorUiState.Content(updatedWork) }
        }
    }

    fun onSaveClick() {
        val state = currentState
        if (state is WorksEditorUiState.Content) {
            launch {
                saveWorkUseCase(state.work.toDomain())
                sendEvent(
                    WorksEditorNavigationEvent.NavigateToWorksList(state.work.hiveId)
                )
            }
        }
    }
}