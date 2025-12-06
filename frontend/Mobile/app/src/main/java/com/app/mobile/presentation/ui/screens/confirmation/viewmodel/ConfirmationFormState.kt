package com.app.mobile.presentation.ui.screens.confirmation.viewmodel

import com.app.mobile.presentation.validators.ValidationError

data class ConfirmationFormState(
    val code: String = "",
    val codeError: ValidationError? = null
)

fun ConfirmationFormState.hasAnyError(): Boolean {
    return codeError != null
}
