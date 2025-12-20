package com.app.mobile.presentation.ui.screens.aboutapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AboutAppViewModel() : ViewModel() {

    private val _aboutAppUiState = MutableStateFlow<AboutAppUiState>(AboutAppUiState.Content)
    val aboutAppUiState = _aboutAppUiState.asStateFlow()

    private val _navigationEvent = Channel<AboutAppNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val handler = CoroutineExceptionHandler { _, exception ->
        _aboutAppUiState.value = AboutAppUiState.Error(exception.message ?: "Unknown error")
        Log.e("AboutAppViewModel", exception.message.toString())
    }

    fun onBackClick() {
        viewModelScope.launch(handler) {
            _navigationEvent.send(AboutAppNavigationEvent.NavigateBack)
        }
    }
}