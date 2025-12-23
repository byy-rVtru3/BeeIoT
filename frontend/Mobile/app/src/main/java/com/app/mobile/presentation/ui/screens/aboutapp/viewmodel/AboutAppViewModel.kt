package com.app.mobile.presentation.ui.screens.aboutapp.viewmodel

import android.util.Log
import com.app.mobile.presentation.ui.components.BaseViewModel

class AboutAppViewModel() :
    BaseViewModel<AboutAppUiState, AboutAppNavigationEvent>(AboutAppUiState.Content) {

    override fun handleError(exception: Throwable) {
        AboutAppUiState.Error(exception.message ?: "Unknown error")
        Log.e("AboutAppViewModel", exception.message.toString())
    }

    fun onBackClick() {
        launch {
            sendEvent(AboutAppNavigationEvent.NavigateBack)
        }
    }
}