package com.app.mobile.presentation.models

sealed interface LogoutResultUi {
    data object Success : LogoutResultUi
    data class Error(val message: String) : LogoutResultUi
}