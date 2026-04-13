package com.app.mobile.presentation.ui.screens.howtouse.viewmodel

import android.util.Log
import com.app.mobile.presentation.ui.components.BaseViewModel

class HowToUseViewModel() :
    BaseViewModel<HowToUseUiState, HowToUseEvent>(HowToUseUiState.Content) {

    override fun handleError(exception: Throwable) {
        HowToUseUiState.Error(exception.message ?: "Unknown error")
        Log.e("HowToUseViewModel", exception.message.toString())
    }

    fun onBackClick() {
        launch {
            sendEvent(HowToUseEvent.NavigateBack)
        }
    }

    fun resetError() = onBackClick()
}
