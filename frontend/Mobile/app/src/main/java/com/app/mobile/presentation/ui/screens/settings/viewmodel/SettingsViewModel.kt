package com.app.mobile.presentation.ui.screens.settings.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler

class SettingsViewModel(

) : ViewModel() {
    private val _settingsUiState = MutableLiveData<SettingsUiState>()
    val settingsUiState: LiveData<SettingsUiState> = _settingsUiState

    private val _navigationEvent = MutableLiveData<SettingsNavigationEvent?>()
    val navigationEvent: LiveData<SettingsNavigationEvent?> = _navigationEvent

    private val handler = CoroutineExceptionHandler { _, exception ->
        _settingsUiState.value = SettingsUiState.Error(exception.message ?: "Unknown error")
        Log.e("SettingsViewModel", exception.message.toString())
    }

    fun onAccountInfoClick() {
        _navigationEvent.value = SettingsNavigationEvent.NavigateToAccountInfo
    }

    fun onLogoutClick() {
        _navigationEvent.value = SettingsNavigationEvent.NavigateToAuthorization
        TODO("Need to implement logout")
    }

    fun onAboutAppClick() {
        _navigationEvent.value = SettingsNavigationEvent.NavigateToAboutApp
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}