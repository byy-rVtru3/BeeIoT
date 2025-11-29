package com.app.mobile.presentation.ui.screens.settings.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.account.LogoutAccountUseCase
import com.app.mobile.presentation.models.account.LogoutResultUi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val logoutUseCase: LogoutAccountUseCase
) : ViewModel() {
    private val _settingsUiState = MutableLiveData<SettingsUiState>(SettingsUiState.Content)
    val settingsUiState: LiveData<SettingsUiState> = _settingsUiState

    private val _navigationEvent = MutableLiveData<SettingsNavigationEvent?>()
    val navigationEvent: LiveData<SettingsNavigationEvent?> = _navigationEvent

    private val handler = CoroutineExceptionHandler { _, exception ->
        _settingsUiState.value = SettingsUiState.Error(exception.message ?: "Unknown error")
        Log.e("SettingsViewModel", exception.message.toString())
    }

    fun onAccountInfoClick() {
        val currentState = _settingsUiState.value
        if (currentState is SettingsUiState.Content) {
            _navigationEvent.value = SettingsNavigationEvent.NavigateToAccountInfo
        }
    }

    fun onLogoutClick() {
        val currentState = _settingsUiState.value
        if (currentState is SettingsUiState.Content) {
            _settingsUiState.value = SettingsUiState.Loading
            viewModelScope.launch(handler) {

                when (val result = logoutUseCase().toUiModel()) {
                    is LogoutResultUi.Success -> {
                        _navigationEvent.value = SettingsNavigationEvent.NavigateToAuthorization
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
            _navigationEvent.value = SettingsNavigationEvent.NavigateToAboutApp
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}