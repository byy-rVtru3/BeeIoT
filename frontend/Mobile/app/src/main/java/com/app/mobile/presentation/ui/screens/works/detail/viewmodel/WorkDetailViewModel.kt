package com.app.mobile.presentation.ui.screens.works.detail.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
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
            when (val result = getWorkUseCase(workId)) {
                is ApiResult.Success -> {
                    val work = result.data
                    if (work != null) {
                        updateState { WorkDetailUiState.Content(work.toUiModel()) }
                    } else {
                        updateState { WorkDetailUiState.Error("Работа не найдена") }
                    }
                }
                else -> updateState { WorkDetailUiState.Error(result.toErrorMessage()) }
            }
        }
    }

    fun refresh() {
        val current = currentState as? WorkDetailUiState.Content ?: return
        updateState { current.copy(isRefreshing = true) }
        launch {
            when (val result = getWorkUseCase(workId)) {
                is ApiResult.Success -> {
                    val work = result.data
                    if (work != null) {
                        updateState { WorkDetailUiState.Content(work.toUiModel()) }
                    } else {
                        updateState { current.copy(isRefreshing = false) }
                    }
                }
                else -> updateState { WorkDetailUiState.Error(result.toErrorMessage()) }
            }
        }
    }

    fun resetError() = loadWork()

    fun onEditClick() {
        launch { sendEvent(WorkDetailEvent.NavigateToEdit(workId, hiveId)) }
    }

    fun onDeleteClick() {
        launch {
            when (val result = deleteWorkUseCase(workId)) {
                is ApiResult.Success -> sendEvent(WorkDetailEvent.NavigateBack(hiveId))
                else -> sendEvent(WorkDetailEvent.ShowSnackBar(result.toErrorMessage()))
            }
        }
    }
}
