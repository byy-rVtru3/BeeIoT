package com.app.mobile.presentation.ui.screens.aboutapp.viewmodel

sealed class AboutAppNavigationEvent {
    object NavigateBack : AboutAppNavigationEvent()
}