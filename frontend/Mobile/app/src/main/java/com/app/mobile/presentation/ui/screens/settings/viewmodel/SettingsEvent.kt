package com.app.mobile.presentation.ui.screens.settings.viewmodel

sealed interface SettingsEvent {
	data object NavigateToAccountInfo : SettingsEvent
	data object NavigateToAboutApp : SettingsEvent
	data object NavigateToAuthorization : SettingsEvent

	data class ShowSnackBar(val message: String) : SettingsEvent
}
