package com.app.mobile.presentation.ui.screens.howtouse.viewmodel

sealed interface HowToUseUiState {
    data object Content : HowToUseUiState
    data object Loading : HowToUseUiState
    data class Error(val message: String) : HowToUseUiState
}
