package com.app.mobile.presentation.ui.screens.aboutapp.viewmodel

sealed interface AboutAppUiState {
    data object Content : AboutAppUiState
    data object Loading : AboutAppUiState
    data class Error(val message: String) : AboutAppUiState
}