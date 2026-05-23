package com.app.mobile.presentation.ui.screens.howtouse.viewmodel

sealed interface HowToUseEvent {
    data object NavigateBack : HowToUseEvent

    data class ShowSnackBar(val message: String) : HowToUseEvent
}
