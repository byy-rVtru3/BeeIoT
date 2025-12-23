package com.app.mobile.presentation.ui.screens.hive.list.vewmodel

import android.util.Log
import com.app.mobile.domain.mappers.toHivePreview
import com.app.mobile.domain.usecase.hives.hive.GetHivesPreviewUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HivesListViewModel(
    private val getHivesPreviewUseCase: GetHivesPreviewUseCase
) : BaseViewModel<HivesListUiState, HivesListNavigationEvent>(HivesListUiState.Loading) {
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()
    override fun handleError(exception: Throwable) {
        updateState { HivesListUiState.Error(exception.message ?: "Unknown error") }
        Log.e("HivesListViewModel", exception.message.toString())
    }

    // Метод для переключения вкладки
    fun onTabSelected(index: Int) {
        _selectedTab.value = index

        // В будущем, когда будет реальный бэкенд для архива,
        // здесь можно добавить логику:
        // if (index == 0) loadActiveHives() else loadArchivedHives()
    }

    fun loadHives() {
        updateState { HivesListUiState.Loading }
        launch {
            val hives = getHivesPreviewUseCase().map { it.toHivePreview() }
            if (hives.isEmpty()) {
                updateState { HivesListUiState.Empty }
            } else {
                updateState { HivesListUiState.Content(hives) }
            }
        }
    }

    fun onRetry() {
        loadHives()
    }

    fun onHiveClick(hiveId: String) {
        if (currentState is HivesListUiState.Content) {
            updateState { HivesListUiState.Loading }
            sendEvent(HivesListNavigationEvent.NavigateToHive(hiveId))
        }
    }

    fun onCreateHiveClick() {
        if (currentState is HivesListUiState.Content || currentState is HivesListUiState.Empty) {
            updateState { HivesListUiState.Loading }
            sendEvent(HivesListNavigationEvent.NavigateToCreateHive)
        }
    }
}