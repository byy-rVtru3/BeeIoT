package com.app.mobile.domain.models.confirmation

data class ConfirmationModel(
    val email: String,
    val code: String,
    val type: TypeConfirmation
)
