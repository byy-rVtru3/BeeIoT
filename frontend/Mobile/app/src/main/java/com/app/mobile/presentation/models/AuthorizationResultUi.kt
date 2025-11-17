package com.app.mobile.presentation.models

sealed interface AuthorizationResultUi {
    object Success : AuthorizationResultUi

    data class Error(val message: String) : AuthorizationResultUi
}