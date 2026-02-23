package com.app.mobile.presentation.ui.screens.authorization.viewmodel

sealed interface AuthorizationEvent {
	data object NavigateToMainScreen : AuthorizationEvent
	data object NavigateToRegistration : AuthorizationEvent

	data class ShowSnackBar(val message: String) : AuthorizationEvent
}