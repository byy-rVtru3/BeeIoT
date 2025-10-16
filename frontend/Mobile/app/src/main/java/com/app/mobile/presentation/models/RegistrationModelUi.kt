package com.app.mobile.presentation.models

data class RegistrationModelUi(
    val userId: String = "",
    val name: String,
    val email: String,
    val password: String,
    val repeatPassword: String
)
