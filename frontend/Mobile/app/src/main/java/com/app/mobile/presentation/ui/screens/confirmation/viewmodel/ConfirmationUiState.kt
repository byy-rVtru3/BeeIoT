package com.app.mobile.presentation.ui.screens.confirmation.viewmodel

import com.app.mobile.presentation.models.ConfirmationModelUi

sealed interface ConfirmationUiState {
    data object Loading : ConfirmationUiState

    data class Error(val message: String) : ConfirmationUiState

    data class Content(val confirmationModelUi: ConfirmationModelUi) : ConfirmationUiState
}