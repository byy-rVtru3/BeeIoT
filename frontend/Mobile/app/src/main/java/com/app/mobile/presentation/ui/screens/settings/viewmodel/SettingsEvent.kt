package com.app.mobile.presentation.ui.screens.settings.viewmodel

sealed interface SettingsEvent {
	data object NavigateToAccountInfo : SettingsEvent
	data object NavigateToAboutApp : SettingsEvent
	data object NavigateToHowToUse : SettingsEvent
	data object NavigateToAuthorization : SettingsEvent
	data object NavigateToNotificationSettings : SettingsEvent

	data class ShowSnackBar(val message: String) : SettingsEvent
}
