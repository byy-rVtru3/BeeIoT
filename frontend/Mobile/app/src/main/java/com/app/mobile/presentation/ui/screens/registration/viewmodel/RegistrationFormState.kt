package com.app.mobile.presentation.ui.screens.registration.viewmodel

import com.app.mobile.presentation.validators.ValidationError

data class RegistrationFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val nameError: ValidationError? = null,
    val emailError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val repeatPasswordError: ValidationError? = null
)

fun RegistrationFormState.hasAnyError(): Boolean {
    return nameError != null ||
            emailError != null ||
            passwordError != null ||
            repeatPasswordError != null
}
