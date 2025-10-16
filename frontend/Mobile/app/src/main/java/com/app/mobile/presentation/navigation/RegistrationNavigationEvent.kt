package com.app.mobile.presentation.navigation

sealed class RegistrationNavigationEvent {
    data class NavigateToConfirmation(val email: String,
                                      val type: String) : RegistrationNavigationEvent()

}