package com.app.mobile.presentation.ui.screens.works.detail.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.hives.works.DeleteWorkUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorkUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.works.detail.WorkDetailRoute

class WorkDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getWorkUseCase: GetWorkUseCase,
    private val deleteWorkUseCase: DeleteWorkUseCase,
) : BaseViewModel<WorkDetailUiState, WorkDetailEvent>(WorkDetailUiState.Loading) {

    private val route = savedStateHandle.toRoute<WorkDetailRoute>()
    private val workId = route.workId
    private val hiveId = route.hiveId

    override fun handleError(exception: Throwable) {
        updateState { WorkDetailUiState.Error(exception.message ?: "Unknown error") }
        Log.e("WorkDetailViewModel", exception.message.toString())
    }

    fun loadWork() {
        updateState { WorkDetailUiState.Loading }
        launch {
            val work = getWorkUseCase(workId)
            if (work != null) {
                updateState { WorkDetailUiState.Content(work.toUiModel()) }
            } else {
                updateState { WorkDetailUiState.Error("Работа не найдена") }
            }
        }
    }

    fun refresh() {
        val current = currentState as? WorkDetailUiState.Content ?: return
        updateState { current.copy(isRefreshing = true) }
        launch {
            val work = getWorkUseCase(workId)
            if (work != null) {
                updateState { WorkDetailUiState.Content(work.toUiModel()) }
            } else {
                updateState { current.copy(isRefreshing = false) }
            }
        }
    }

    fun resetError() = loadWork()

    fun onEditClick() {
        launch { sendEvent(WorkDetailEvent.NavigateToEdit(workId, hiveId)) }
    }

    fun onDeleteClick() {
        launch {
            deleteWorkUseCase(workId)
            sendEvent(WorkDetailEvent.NavigateBack(hiveId))
        }
    }
}
