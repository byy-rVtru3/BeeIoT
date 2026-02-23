package com.app.mobile.presentation.ui.screens.accountinfo.viewmodel

sealed interface AccountInfoEvent {
    data object NavigateToRegistration : AccountInfoEvent

    data object NavigateBack : AccountInfoEvent

    data class ShowSnackBar(val message: String) : AccountInfoEvent
}