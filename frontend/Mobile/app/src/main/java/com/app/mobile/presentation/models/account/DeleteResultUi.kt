package com.app.mobile.presentation.models.account

sealed interface DeleteResultUi {
    data object Success : DeleteResultUi
    data class Error(val message: String) : DeleteResultUi
}
