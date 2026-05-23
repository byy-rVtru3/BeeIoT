package com.app.mobile.presentation.ui.screens.hub.list.viewmodel

import com.app.mobile.presentation.models.hive.HubPreviewModel

sealed interface HubsListUiState {
    data class Content(val hubs: List<HubPreviewModel>) : HubsListUiState
    data object Empty : HubsListUiState
    data object Loading : HubsListUiState
    data class Error(val message: String) : HubsListUiState
}
