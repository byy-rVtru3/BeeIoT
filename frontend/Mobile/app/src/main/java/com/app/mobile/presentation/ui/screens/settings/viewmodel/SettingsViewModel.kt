package com.app.mobile.presentation.ui.screens.settings.viewmodel

import android.util.Log
import com.app.mobile.domain.mappers.toLogoutUiModel
import com.app.mobile.domain.usecase.account.LogoutAccountUseCase
import com.app.mobile.presentation.models.account.LogoutResultUi
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.ui.theme.ThemeManager

class SettingsViewModel(
	private val logoutUseCase: LogoutAccountUseCase,
	private val themeManager: ThemeManager
) : BaseViewModel<SettingsUiState, SettingsEvent>(SettingsUiState.Content()) {

	init {
		observeTheme()
	}

	override fun handleError(exception: Throwable) {
		updateState { SettingsUiState.Error(exception.message ?: "Unknown error") }
		Log.e("SettingsViewModel", exception.message.toString())
	}

	fun onAccountInfoClick() {
		if (currentState is SettingsUiState.Content) {
			sendEvent(SettingsEvent.NavigateToAccountInfo)
		}
	}

	fun onLogoutClick() {
		if (currentState is SettingsUiState.Content) {
			updateState { SettingsUiState.Loading }
			launch {

				when (val result = logoutUseCase().toLogoutUiModel()) {
					is LogoutResultUi.Success -> {
						sendEvent(SettingsEvent.NavigateToAuthorization)
					}

					is LogoutResultUi.Error   -> {
						sendEvent(SettingsEvent.ShowSnackBar(result.message))
						updateState { SettingsUiState.Content() }
					}
				}
			}
		}
	}

	fun resetError() {
		val state = currentState
		if (state is SettingsUiState.Content) {
			updateState { state.copy() }
		} else {
			updateState { SettingsUiState.Content() }
		}
	}

	fun onAboutAppClick() {
		if (currentState is SettingsUiState.Content) {
			sendEvent(SettingsEvent.NavigateToAboutApp)
		}
	}

	private fun observeTheme() {
		launch {
			themeManager.themeState.collect { themeState ->
				val state = currentState
				if (state is SettingsUiState.Content && themeState.isReady) {
					updateState { state.copy(isDarkTheme = themeState.isDarkTheme) }
				}
			}
		}
	}

	fun onToggleTheme() {
		themeManager.toggleTheme()
	}
}