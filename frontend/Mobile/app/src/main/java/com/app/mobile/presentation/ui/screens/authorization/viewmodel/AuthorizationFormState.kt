package com.app.mobile.presentation.ui.screens.authorization.viewmodel

import com.app.mobile.presentation.validators.ValidationError

data class AuthorizationFormState(
    val email: String = "",
    val password: String = "",
    val emailError: ValidationError? = null,
    val passwordError: ValidationError? = null
)

fun AuthorizationFormState.hasAnyError(): Boolean {
    return emailError != null || passwordError != null
}

