package com.app.mobile.presentation.models

import com.app.mobile.presentation.validators.ValidationError

data class ConfirmationModelUi(
    val email: String,
    val code: String,
    val type: String,
    val codeError: ValidationError? = null
)