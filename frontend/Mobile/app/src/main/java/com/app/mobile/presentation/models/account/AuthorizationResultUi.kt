package com.app.mobile.presentation.models.account

sealed interface AuthorizationResultUi {
    object Success : AuthorizationResultUi

    data class Error(val message: String) : AuthorizationResultUi
}