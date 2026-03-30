package com.app.mobile.presentation.ui.screens.hive.list.vewmodel

import android.util.Log
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.mappers.toHivePreview
import com.app.mobile.domain.usecase.hives.hive.DeleteHiveUseCase
import com.app.mobile.domain.usecase.hives.hive.GetHivesPreviewUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HivesListViewModel(
    private val getHivesPreviewUseCase: GetHivesPreviewUseCase,
    private val deleteHiveUseCase: DeleteHiveUseCase
) : BaseViewModel<HivesListUiState, HivesListEvent>(HivesListUiState.Loading) {
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()
    override fun handleError(exception: Throwable) {
        updateState { HivesListUiState.Error(exception.message ?: "Unknown error") }
        Log.e("HivesListViewModel", exception.message.toString())
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    fun loadHives() {
        updateState { HivesListUiState.Loading }
        launch {
            when (val result = getHivesPreviewUseCase()) {
                is ApiResult.Success -> {
                    val hives = result.data.map { it.toHivePreview() }
                    if (hives.isEmpty()) {
                        updateState { HivesListUiState.Empty }
                    } else {
                        updateState { HivesListUiState.Content(hives) }
                    }
                }

                else -> {
                    updateState { HivesListUiState.Error(result.toErrorMessage()) }
                }
            }
        }
    }

    fun refresh() {
        val current = currentState as? HivesListUiState.Content ?: return
        updateState { current.copy(isRefreshing = true) }
        launch {
            when (val result = getHivesPreviewUseCase()) {
                is ApiResult.Success -> {
                    val hives = result.data.map { it.toHivePreview() }
                    if (hives.isEmpty()) {
                        updateState { HivesListUiState.Empty }
                    } else {
                        updateState { HivesListUiState.Content(hives) }
                    }
                }
                else -> updateState { HivesListUiState.Error(result.toErrorMessage()) }
            }
        }
    }

    fun onRetry() {
        loadHives()
    }

    fun onHiveClick(hiveName: String) {
        if (currentState is HivesListUiState.Content) {
            updateState { HivesListUiState.Loading }
            sendEvent(HivesListEvent.NavigateToHive(hiveName))
        }
    }

    fun onCreateHiveClick() {
        if (currentState is HivesListUiState.Content || currentState is HivesListUiState.Empty) {
            updateState { HivesListUiState.Loading }
            sendEvent(HivesListEvent.NavigateToCreateHive)
        }
    }

    fun onDeleteHive(name: String) {
        val current = currentState as? HivesListUiState.Content ?: return
        val updated = current.hives.filter { it.name != name }
        updateState { if (updated.isEmpty()) HivesListUiState.Empty else current.copy(hives = updated) }
        launch {
            when (val result = deleteHiveUseCase(name)) {
                is ApiResult.Success -> Unit
                else -> {
                    sendEvent(HivesListEvent.ShowSnackBar(result.toErrorMessage()))
                    loadHives()
                }
            }
        }
    }
}
