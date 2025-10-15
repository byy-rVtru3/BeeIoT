package com.app.mobile.domain.models.registration

data class RegistrationModel(
    val email: String,
    val name: String,
    val password: String,
    val repeatPassword: String
)
