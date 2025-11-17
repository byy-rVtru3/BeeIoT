package com.app.mobile.presentation.models

sealed interface RegistrationResultUi {
    object Success : RegistrationResultUi
    data class Error(val message: String) : RegistrationResultUi
}