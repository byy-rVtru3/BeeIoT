package com.app.mobile.presentation.ui.screens.aboutapp.viewmodel

sealed class AboutAppUiState {
    object Loading : AboutAppUiState()
    data class Success(val isMockEnabled: Boolean) : AboutAppUiState()
}
