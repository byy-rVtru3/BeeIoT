package com.app.mobile.presentation.models

sealed class RegistrationResultUi {
    object Success : RegistrationResultUi()
    data class Error(val message: String) : RegistrationResultUi()
}