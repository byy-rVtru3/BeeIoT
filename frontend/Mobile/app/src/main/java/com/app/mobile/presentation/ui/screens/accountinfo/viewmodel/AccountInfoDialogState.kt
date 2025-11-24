package com.app.mobile.presentation.ui.screens.accountinfo.viewmodel

sealed interface AccountInfoDialogState {
    data object Hidden : AccountInfoDialogState
    data class SetName(val name: String) : AccountInfoDialogState
    data class SetEmail(val email: String) : AccountInfoDialogState
    data class SetPassword(val password: String) : AccountInfoDialogState
}