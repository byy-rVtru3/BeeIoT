package com.app.mobile.presentation.ui.screens.settings.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.account.LogoutAccountUseCase
import com.app.mobile.presentation.models.account.LogoutResultUi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val logoutUseCase: LogoutAccountUseCase
) : ViewModel() {
    private val _settingsUiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Content)
    val settingsUiState = _settingsUiState.asStateFlow()

    private val _navigationEvent = Channel<SettingsNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val handler = CoroutineExceptionHandler { _, exception ->
        _settingsUiState.value = SettingsUiState.Error(exception.message ?: "Unknown error")
        Log.e("SettingsViewModel", exception.message.toString())
    }

    fun onAccountInfoClick() {
        val currentState = _settingsUiState.value
        if (currentState is SettingsUiState.Content) {
            viewModelScope.launch(handler) {
                _navigationEvent.send(SettingsNavigationEvent.NavigateToAccountInfo)
            }
        }
    }

    fun onLogoutClick() {
        val currentState = _settingsUiState.value
        if (currentState is SettingsUiState.Content) {
            _settingsUiState.value = SettingsUiState.Loading
            viewModelScope.launch(handler) {

                when (val result = logoutUseCase().toUiModel()) {
                    is LogoutResultUi.Success -> {
                        _navigationEvent.send(SettingsNavigationEvent.NavigateToAuthorization)
                    }

                    is LogoutResultUi.Error -> {
                        _settingsUiState.value = SettingsUiState.Error(result.message)

                    }
                }
            }
        }
    }

    fun onAboutAppClick() {
        val currentState = _settingsUiState.value
        if (currentState is SettingsUiState.Content) {
            viewModelScope.launch(handler) {
                _navigationEvent.send(SettingsNavigationEvent.NavigateToAboutApp)
            }
        }
    }
}