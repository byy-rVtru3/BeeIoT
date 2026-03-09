package com.app.mobile.presentation.ui.screens.settings.viewmodel

import android.util.Log
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.account.LogoutAccountUseCase
import com.app.mobile.domain.usecase.notifications.CheckIfNotificationPromptShownUseCase
import com.app.mobile.domain.usecase.notifications.SendPushTokenUseCase
import com.app.mobile.domain.usecase.notifications.SetNotificationPromptShownUseCase
import com.app.mobile.presentation.models.account.LogoutResultUi
import com.app.mobile.presentation.ui.components.BaseViewModel

class SettingsViewModel(
	private val logoutUseCase: LogoutAccountUseCase,
	private val checkIfNotificationPromptShownUseCase: CheckIfNotificationPromptShownUseCase,
	private val setNotificationPromptShownUseCase: SetNotificationPromptShownUseCase,
	private val sendPushTokenUseCase: SendPushTokenUseCase
) : BaseViewModel<SettingsUiState, SettingsEvent>(SettingsUiState.Content()) {

	init {
		checkNotificationPrompt()
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

				when (val result = logoutUseCase().toUiModel()) {
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

	fun checkNotificationPrompt() {
		if (currentState is SettingsUiState.Content) {
			launch {
				val hasShown = !checkIfNotificationPromptShownUseCase()
				updateState { SettingsUiState.Content(showNotificationPrompt = hasShown) }
			}
		}
	}

	fun onAcceptNotificationPrompt() {
		if (currentState is SettingsUiState.Content) {
			updateState { SettingsUiState.Content(showNotificationPrompt = false) }
			launch {
				setNotificationPromptShownUseCase(true)
			}
			sendPushToken()
		}
	}

	fun onDeclineNotificationPrompt() {
		if (currentState is SettingsUiState.Content) {
			updateState { SettingsUiState.Content(showNotificationPrompt = false) }
			launch {
				setNotificationPromptShownUseCase(false)
			}
		}
	}

	fun sendPushToken() {
		val state = currentState
		if (state is SettingsUiState.Content) {
			if (state.showNotificationPrompt) {
				launch { sendPushTokenUseCase() }
			}
		}
	}

	fun resetError() {
		updateState { SettingsUiState.Content() }
	}

	fun onAboutAppClick() {
		if (currentState is SettingsUiState.Content) {
			sendEvent(SettingsEvent.NavigateToAboutApp)
		}
	}
}