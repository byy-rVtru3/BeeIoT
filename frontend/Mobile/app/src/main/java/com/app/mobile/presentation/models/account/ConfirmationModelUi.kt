package com.app.mobile.presentation.models.account

data class ConfirmationModelUi(
    val email: String,
    val code: String,
    val type: TypeConfirmationUi
)