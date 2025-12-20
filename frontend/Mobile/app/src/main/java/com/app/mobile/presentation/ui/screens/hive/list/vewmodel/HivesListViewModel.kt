package com.app.mobile.presentation.ui.screens.hive.list.vewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toHivePreview
import com.app.mobile.domain.usecase.hives.hive.GetHivesPreviewUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HivesListViewModel(
    private val getHivesPreviewUseCase: GetHivesPreviewUseCase
) : ViewModel() {

    private val _hivesListUiState = MutableStateFlow<HivesListUiState>(HivesListUiState.Loading)
    val hivesListUiState = _hivesListUiState.asStateFlow()


    private val _navigationEvent = Channel<HivesListNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

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
                _navigationEvent.send(HivesListNavigationEvent.NavigateToHive(hiveId))
            }
        }
    }

    fun onCreateHiveClick() {
        val currentState = _hivesListUiState.value
        if (currentState is HivesListUiState.Content || currentState is HivesListUiState.Empty) {
            _hivesListUiState.value = HivesListUiState.Loading
            viewModelScope.launch(handler) {
                _navigationEvent.send(HivesListNavigationEvent.NavigateToCreateHive)
            }
        }
    }
}