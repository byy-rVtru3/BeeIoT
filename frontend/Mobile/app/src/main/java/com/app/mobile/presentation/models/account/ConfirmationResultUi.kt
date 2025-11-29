package com.app.mobile.presentation.models.account

sealed interface ConfirmationResultUi {
    data object Success : ConfirmationResultUi
    data class Error(val message: String) : ConfirmationResultUi
}