package com.app.mobile.presentation.ui.screens.works.list.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.hives.works.DeleteWorkUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorksUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.works.list.WorksListRoute

class WorksListViewModel(
	savedStateHandle: SavedStateHandle,
	private val getWorksUseCase: GetWorksUseCase,
	private val deleteWorkUseCase: DeleteWorkUseCase
) : BaseViewModel<WorksListUiState, WorksListEvent>(WorksListUiState.Loading) {

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
			when (val result = getWorksUseCase(hiveId)) {
				is ApiResult.Success -> updateState { WorksListUiState.Content(result.data.map { it.toUiModel() }) }
				else -> updateState { WorksListUiState.Error(result.toErrorMessage()) }
			}
		}
	}

	fun refresh() {
		val current = currentState as? WorksListUiState.Content ?: return
		updateState { current.copy(isRefreshing = true) }
		launch {
			when (val result = getWorksUseCase(hiveId)) {
				is ApiResult.Success -> updateState { WorksListUiState.Content(result.data.map { it.toUiModel() }) }
				else -> updateState { WorksListUiState.Error(result.toErrorMessage()) }
			}
		}
	}

	fun resetError() = loadWorks()

	fun onCreateClick() {
		if (currentState is WorksListUiState.Content) {
			sendEvent(WorksListEvent.NavigateToWorkCreate(hiveId))
		}
	}

	fun onWorkClick(workId: String) {
		if (currentState is WorksListUiState.Content) {
			sendEvent(WorksListEvent.NavigateToWorkDetail(workId, hiveId))
		}
	}

	fun onDeleteWork(workId: String) {
		val current = currentState as? WorksListUiState.Content ?: return
		val updated = current.works.filter { it.id != workId }
		updateState { current.copy(works = updated) }
		launch {
			when (val result = deleteWorkUseCase(workId)) {
				is ApiResult.Success -> Unit
				else -> {
					sendEvent(WorksListEvent.ShowSnackBar(result.toErrorMessage()))
					loadWorks()
				}
			}
		}
	}
}
