package com.app.mobile.presentation.models.account

sealed interface RegistrationResultUi {
    object Success : RegistrationResultUi
    data class Error(val message: String) : RegistrationResultUi
}