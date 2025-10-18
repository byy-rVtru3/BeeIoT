package com.app.mobile.presentation.ui.screens.confirmation.models

data class ConfirmationActions(
    val onCodeChange: (String) -> Unit,
    val onConfirmClick: () -> Unit,
    val onResendCodeClick: () -> Unit
)
