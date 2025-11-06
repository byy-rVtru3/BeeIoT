package com.app.mobile.presentation.models

sealed interface ConfirmationResultUi {
    data object Success : ConfirmationResultUi
    data class Error(val message: String) : ConfirmationResultUi
}