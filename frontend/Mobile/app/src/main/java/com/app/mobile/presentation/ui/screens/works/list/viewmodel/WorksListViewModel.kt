package com.app.mobile.presentation.ui.screens.works.list.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.hives.works.GetWorksUseCase
import com.app.mobile.presentation.ui.screens.works.list.WorksListRoute
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class WorksListViewModel(
    savedStateHandle: SavedStateHandle,
    private val getWorksUseCase: GetWorksUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<WorksListRoute>()
    private val hiveId = route.hiveId

    private val _worksListUiState = MutableStateFlow<WorksListUiState>(WorksListUiState.Loading)
    val worksListUiState = _worksListUiState.asStateFlow()

    private val _navigationEvent = Channel<WorksListNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val handler = CoroutineExceptionHandler { _, exception ->
        _worksListUiState.value = WorksListUiState.Error(exception.message ?: "Unknown error")
        Log.e("WorksListViewModel", exception.message.toString())
    }

    fun loadWorks() {
        if (_worksListUiState.value !is WorksListUiState.Content) {
            _worksListUiState.value = WorksListUiState.Loading
        }
        viewModelScope.launch(handler) {
            val works = getWorksUseCase(hiveId).map { it.toUiModel() }
            _worksListUiState.value = WorksListUiState.Content(works)
        }
    }

    fun onCreateClick() {
        val currentState = _worksListUiState.value
        if (currentState is WorksListUiState.Content) {
            viewModelScope.launch {
                _navigationEvent.send(WorksListNavigationEvent.NavigateToWorkCreate(hiveId))
            }
        }
    }

    fun onWorkClick(workId: String) {
        val currentState = _worksListUiState.value
        if (currentState is WorksListUiState.Content) {
            viewModelScope.launch {
                _navigationEvent.send(WorksListNavigationEvent.NavigateToWorkEditor(workId, hiveId))
            }
        }
    }
}