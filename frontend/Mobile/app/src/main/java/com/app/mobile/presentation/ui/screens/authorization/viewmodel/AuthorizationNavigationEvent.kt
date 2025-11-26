package com.app.mobile.presentation.ui.screens.authorization.viewmodel

sealed class AuthorizationNavigationEvent {
    data object NavigateToMainScreen : AuthorizationNavigationEvent()
    data object NavigateToRegistration : AuthorizationNavigationEvent()
}