package com.app.mobile.presentation.ui.screens.aboutapp.viewmodel

sealed interface AboutAppEvent {
    data object NavigateBack : AboutAppEvent

    data class ShowSnackBar(val message: String) : AboutAppEvent
}