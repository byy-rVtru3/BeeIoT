package com.app.mobile.presentation.ui.screens.authorization.models

data class AuthorizationActions(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onAuthorizeClick: () -> Unit,
    val onRegistrationClick: () -> Unit
)
