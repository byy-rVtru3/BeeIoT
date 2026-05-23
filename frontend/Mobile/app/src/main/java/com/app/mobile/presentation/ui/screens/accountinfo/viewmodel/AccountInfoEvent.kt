package com.app.mobile.presentation.ui.screens.accountinfo.viewmodel

import com.app.mobile.presentation.models.account.TypeConfirmationUi

sealed interface AccountInfoEvent {
    data object NavigateToRegistration : AccountInfoEvent

    data object NavigateBack : AccountInfoEvent

    data class ShowSnackBar(val message: String) : AccountInfoEvent

    data class NavigateToConfirmation(
        val email: String,
        val type: TypeConfirmationUi
    ) : AccountInfoEvent
}