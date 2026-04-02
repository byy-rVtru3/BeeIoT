package com.app.mobile.presentation.ui.screens.works.editor.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.hives.works.AddWorkUseCase
import com.app.mobile.domain.usecase.hives.works.CreateWorkUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorkUseCase
import com.app.mobile.domain.usecase.hives.works.UpdateWorkUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.works.editor.WorkEditorRoute

class WorksEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val createWorkUseCase: CreateWorkUseCase,
    private val getWorkUseCase: GetWorkUseCase,
    private val addWorkUseCase: AddWorkUseCase,
    private val updateWorkUseCase: UpdateWorkUseCase
) : BaseViewModel<WorksEditorUiState, WorksEditorEvent>(WorksEditorUiState.Loading) {

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
            if (workId != null) {
                when (val result = getWorkUseCase(workId)) {
                    is ApiResult.Success -> {
                        val work = result.data ?: createWorkUseCase(hiveId)
                        updateState { WorksEditorUiState.Content(work.toUiModel()) }
                    }
                    else -> updateState { WorksEditorUiState.Error(result.toErrorMessage()) }
                }
            } else {
                val work = createWorkUseCase(hiveId)
                updateState { WorksEditorUiState.Content(work.toUiModel()) }
            }
        }
    }

    fun onTitleChange(title: String) {
        val state = currentState
        if (state is WorksEditorUiState.Content) {
            updateState { WorksEditorUiState.Content(state.work.copy(title = title)) }
        }
    }

    fun onTextChange(text: String) {
        val state = currentState
        if (state is WorksEditorUiState.Content) {
            updateState { WorksEditorUiState.Content(state.work.copy(text = text)) }
        }
    }

    fun onSaveClick() {
        val state = currentState
        if (state is WorksEditorUiState.Content) {
            launch {
                val work = state.work.toDomain()
                val result = if (workId == null) addWorkUseCase(work) else updateWorkUseCase(work)
                when (result) {
                    is ApiResult.Success -> sendEvent(WorksEditorEvent.NavigateToWorksList(state.work.hiveId))
                    else -> sendEvent(WorksEditorEvent.ShowSnackBar(result.toErrorMessage()))
                }
            }
        }
    }

    fun resetError() = loadWork()
}
