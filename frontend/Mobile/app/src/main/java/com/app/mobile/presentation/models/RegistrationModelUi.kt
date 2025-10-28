package com.app.mobile.presentation.models

import com.app.mobile.presentation.validators.ValidationError

data class RegistrationModelUi(
    val name: String,
    val email: String,
    val password: String,
    val repeatPassword: String,
    val nameError: ValidationError? = null,
    val emailError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val repeatPasswordError: ValidationError? = null
)
