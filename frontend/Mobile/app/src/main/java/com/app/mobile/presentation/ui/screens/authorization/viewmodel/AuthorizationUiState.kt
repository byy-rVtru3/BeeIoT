package com.app.mobile.presentation.ui.screens.authorization.viewmodel

import com.app.mobile.presentation.models.account.AuthorizationModelUi

sealed interface AuthorizationUiState {
    data object Loading : AuthorizationUiState

    data class Error(val message: String) : AuthorizationUiState

    data class Content(
        val authorizationModelUi: AuthorizationModelUi,
        val formState: AuthorizationFormState = AuthorizationFormState()
    ) : AuthorizationUiState
}