package com.app.mobile.presentation.ui.screens.registration.viewmodel

import com.app.mobile.presentation.models.account.TypeConfirmationUi

sealed class RegistrationNavigationEvent {
    data class NavigateToConfirmation(
        val email: String,
        val type: TypeConfirmationUi
    ) : RegistrationNavigationEvent()

}