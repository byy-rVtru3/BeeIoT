package com.app.mobile.presentation.models

sealed interface DeleteResultUi {
    data object Success : DeleteResultUi
    data class Error(val message: String) : DeleteResultUi
}
