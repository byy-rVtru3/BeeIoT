package com.app.mobile.presentation.ui.screens.settings.models

data class SettingsActions(
    val onLogoutClick: () -> Unit,
    val onAccountInfoClick: () -> Unit,
    val onAboutAppClick: () -> Unit,
)
