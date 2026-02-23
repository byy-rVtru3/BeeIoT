package com.app.mobile.presentation.ui.screens.registration.viewmodel

import com.app.mobile.presentation.models.account.TypeConfirmationUi

sealed interface RegistrationEvent {
	data class NavigateToConfirmation(
		val email: String,
		val type: TypeConfirmationUi
	) : RegistrationEvent

	data class ShowSnackBar(val message: String) : RegistrationEvent
}