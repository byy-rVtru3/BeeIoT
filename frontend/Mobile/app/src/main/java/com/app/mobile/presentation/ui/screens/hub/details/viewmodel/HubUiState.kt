package com.app.mobile.presentation.ui.screens.hub.details.viewmodel

import com.app.mobile.presentation.models.hub.HubDetailUi

sealed interface HubUiState {
    data object Loading : HubUiState
    data class Error(val message: String) : HubUiState
    data class Content(val hub: HubDetailUi) : HubUiState
}
