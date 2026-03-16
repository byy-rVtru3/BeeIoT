package com.app.mobile.presentation.ui.screens.hub.list.viewmodel

import android.util.Log
import com.app.mobile.domain.mappers.toPreviewModel
import com.app.mobile.domain.usecase.hives.hub.GetHubsUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HubsListViewModel(
    private val getHubsUseCase: GetHubsUseCase
) : BaseViewModel<HubsListUiState, HubsListEvent>(HubsListUiState.Loading) {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab = _selectedTab.asStateFlow()

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
            val hubs = getHubsUseCase().map { it.toPreviewModel() }
            if (hubs.isEmpty()) {
                updateState { HubsListUiState.Empty }
            } else {
                updateState { HubsListUiState.Content(hubs) }
            }
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
}
