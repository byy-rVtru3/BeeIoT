package com.app.mobile.presentation.ui.screens.confirmation.viewmodel

sealed class ConfirmationNavigationEvent {
    data object NavigateToAuthorization : ConfirmationNavigationEvent()
}
