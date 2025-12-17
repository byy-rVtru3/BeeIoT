package com.app.mobile.presentation.ui.screens.accountinfo.models

data class AccountInfoActions(
    val onNameClick: () -> Unit,
    val onEmailClick: () -> Unit,
    val onPasswordClick: () -> Unit,
    val onDeleteClick: () -> Unit
)
