package com.app.mobile.presentation.navigation

import com.app.mobile.presentation.models.TypeConfirmationUi

sealed class RegistrationNavigationEvent {
    data class NavigateToConfirmation(
        val email: String,
        val type: TypeConfirmationUi
    ) : RegistrationNavigationEvent()

}