package com.app.mobile.presentation.ui.screens.works.list.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.hives.works.GetWorksUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.works.list.WorksListRoute

class WorksListViewModel(
    savedStateHandle: SavedStateHandle,
    private val getWorksUseCase: GetWorksUseCase
) : BaseViewModel<WorksListUiState, WorksListNavigationEvent>(WorksListUiState.Loading) {

    private val route = savedStateHandle.toRoute<WorksListRoute>()
    private val hiveId = route.hiveId

    override fun handleError(exception: Throwable) {
        updateState { WorksListUiState.Error(exception.message ?: "Unknown error") }
        Log.e("WorksListViewModel", exception.message.toString())
    }

    fun loadWorks() {
        if (currentState !is WorksListUiState.Content) {
            updateState { WorksListUiState.Loading }
        }
        launch {
            val works = getWorksUseCase(hiveId).map { it.toUiModel() }
            updateState { WorksListUiState.Content(works) }
        }
    }

    fun onCreateClick() {
        if (currentState is WorksListUiState.Content) {
            sendEvent(WorksListNavigationEvent.NavigateToWorkCreate(hiveId))
        }
    }

    fun onWorkClick(workId: String) {
        if (currentState is WorksListUiState.Content) {
            sendEvent(WorksListNavigationEvent.NavigateToWorkEditor(workId, hiveId))
        }
    }
}