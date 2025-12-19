package com.app.mobile.presentation.ui.screens.works.list.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.hives.works.GetWorksUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class WorksListViewModel(
    private val getWorksUseCase: GetWorksUseCase
) : ViewModel() {

    private val _worksListUiState =
        MutableLiveData<WorksListUiState>(WorksListUiState.Loading)
    val worksListUiState: LiveData<WorksListUiState> = _worksListUiState

    private val _navigationEvent = MutableLiveData<WorksListNavigationEvent?>()
    val navigationEvent: LiveData<WorksListNavigationEvent?> = _navigationEvent

    private val handler = CoroutineExceptionHandler { _, exception ->
        _worksListUiState.value = WorksListUiState.Error(exception.message ?: "Unknown error")
        Log.e("WorksListViewModel", exception.message.toString())
    }

    fun loadWorks(hiveId: String) {
        _worksListUiState.value = WorksListUiState.Loading
        viewModelScope.launch(handler) {
            val works = getWorksUseCase(hiveId).map { it.toUiModel() }
            _worksListUiState.value = WorksListUiState.Content(works)
        }
    }

    fun onCreateClick(hiveId: String) {
        val currentState = _worksListUiState.value
        if (currentState is WorksListUiState.Content) {
            _navigationEvent.value =
                WorksListNavigationEvent.NavigateToWorkCreate(hiveId)
        }
    }

    fun onWorkClick(workId: String) {
        val currentState = _worksListUiState.value
        if (currentState is WorksListUiState.Content) {
            _navigationEvent.value =
                WorksListNavigationEvent.NavigateToWorkEditor(workId)
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}