package com.app.mobile.presentation.ui.screens.confirmation.viewmodel

sealed interface ConfirmationEvent {
    data object NavigateToAuthorization : ConfirmationEvent

    data object NavigateToAccountInfo : ConfirmationEvent

    data class ShowSnackBar(val message: String) : ConfirmationEvent
}
