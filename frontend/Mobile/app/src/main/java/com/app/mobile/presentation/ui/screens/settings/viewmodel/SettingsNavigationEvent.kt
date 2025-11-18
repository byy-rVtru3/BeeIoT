package com.app.mobile.presentation.ui.screens.settings.viewmodel

sealed class SettingsNavigationEvent {
    data object NavigateToAccountInfo : SettingsNavigationEvent()
    data object NavigateToAboutApp : SettingsNavigationEvent()
    data object NavigateToAuthorization : SettingsNavigationEvent()
}
