package com.app.mobile.presentation.ui.screens.hives.list.vewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toHivePreview
import com.app.mobile.domain.usecase.hives.GetHivesPreviewUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class HivesListViewModel(
    private val getHivesPreviewUseCase: GetHivesPreviewUseCase
) : ViewModel() {

    private val _hivesListUiState = MutableLiveData<HivesListUiState>()
    val hivesListUiState: LiveData<HivesListUiState> = _hivesListUiState

    private val _navigationEvent = MutableLiveData<HivesListNavigationEvent?>()
    val navigationEvent: LiveData<HivesListNavigationEvent?> = _navigationEvent

    val handler = CoroutineExceptionHandler { _, exception ->
        _hivesListUiState.value = HivesListUiState.Error(exception.message ?: "Unknown error")
        Log.e("HivesListViewModel", exception.message.toString())
    }

    fun loadHives() {
        _hivesListUiState.value = HivesListUiState.Loading
        viewModelScope.launch(handler) {
            val hives = getHivesPreviewUseCase().map { it.toHivePreview() }
            if (hives.isEmpty()) {
                _hivesListUiState.value = HivesListUiState.Empty
            } else {
                _hivesListUiState.value = HivesListUiState.Content(hives)
            }
        }
    }

    fun onRetry() {
        loadHives()
    }

    fun onHiveClick(hiveId: String) {
        val currentState = _hivesListUiState.value
        if (currentState is HivesListUiState.Content) {
            _hivesListUiState.value = HivesListUiState.Loading
            viewModelScope.launch(handler) {
                _navigationEvent.value = HivesListNavigationEvent.NavigateToHive(hiveId)
            }
        }
    }

    fun onCreateHiveClick() {
        val currentState = _hivesListUiState.value
        if (currentState is HivesListUiState.Content) {
            _hivesListUiState.value = HivesListUiState.Loading
            viewModelScope.launch(handler) {
                _navigationEvent.value = HivesListNavigationEvent.NavigateToCreateHive
            }
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}