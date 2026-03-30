package com.app.mobile.presentation.ui.screens.hub.list.viewmodel

import android.util.Log
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.mappers.toPreviewModel
import com.app.mobile.domain.usecase.hives.hub.DeleteHubUseCase
import com.app.mobile.domain.usecase.hives.hub.GetHubsUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HubsListViewModel(
    private val getHubsUseCase: GetHubsUseCase,
    private val deleteHubUseCase: DeleteHubUseCase
) : BaseViewModel<HubsListUiState, HubsListEvent>(HubsListUiState.Loading) {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    override fun handleError(exception: Throwable) {
        updateState { HubsListUiState.Error(exception.message ?: "Unknown error") }
        Log.e("HubsListViewModel", exception.message.toString())
    }

    fun onTabSelected(index: Int) {
        _selectedTab.value = index
    }

    fun loadHubs() {
        updateState { HubsListUiState.Loading }
        launch {
            when (val result = getHubsUseCase()) {
                is ApiResult.Success -> {
                    val hubs = result.data.map { it.toPreviewModel() }
                    if (hubs.isEmpty()) {
                        updateState { HubsListUiState.Empty }
                    } else {
                        updateState { HubsListUiState.Content(hubs) }
                    }
                }
                else -> {
                    updateState { HubsListUiState.Error(result.toErrorMessage()) }
                }
            }
        }
    }

    fun refreshHubs() {
        _isRefreshing.value = true
        launch {
            when (val result = getHubsUseCase()) {
                is ApiResult.Success -> {
                    val hubs = result.data.map { it.toPreviewModel() }
                    if (hubs.isEmpty()) {
                        updateState { HubsListUiState.Empty }
                    } else {
                        updateState { HubsListUiState.Content(hubs) }
                    }
                }
                else -> {
                    sendEvent(HubsListEvent.ShowSnackBar(result.toErrorMessage()))
                }
            }
            _isRefreshing.value = false
        }
    }

    fun onRetry() {
        loadHubs()
    }

    fun onHubClick(hubId: String) {
        if (currentState is HubsListUiState.Content) {
            updateState { HubsListUiState.Loading }
            sendEvent(HubsListEvent.NavigateToHub(hubId))
        }
    }

    fun onCreateHubClick() {
        if (currentState is HubsListUiState.Content || currentState is HubsListUiState.Empty) {
            updateState { HubsListUiState.Loading }
            sendEvent(HubsListEvent.NavigateToCreateHub)
        }
    }

    fun onDeleteHub(id: String) {
        val current = currentState as? HubsListUiState.Content ?: return
        val updated = current.hubs.filter { it.id != id }
        updateState { if (updated.isEmpty()) HubsListUiState.Empty else current.copy(hubs = updated) }
        launch {
            when (val result = deleteHubUseCase(id)) {
                is ApiResult.Success -> Unit
                else -> {
                    sendEvent(HubsListEvent.ShowSnackBar(result.toErrorMessage()))
                    loadHubs()
                }
            }
        }
    }
}
