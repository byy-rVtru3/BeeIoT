package com.app.mobile.presentation.ui.screens.settings.viewmodel

sealed interface SettingsUiState {
	data class Content(
		val isDarkTheme: Boolean = false
	) : SettingsUiState
	data object Loading : SettingsUiState
	data class Error(val message: String) : SettingsUiState
}