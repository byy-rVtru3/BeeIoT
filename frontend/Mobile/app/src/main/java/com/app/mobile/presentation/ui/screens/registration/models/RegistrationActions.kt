package com.app.mobile.presentation.ui.screens.registration.models

data class RegistrationActions(
    val onEmailChange: (String) -> Unit,
    val onNameChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onRepeatPasswordChange: (String) -> Unit,
    val onRegisterClick: () -> Unit
)
