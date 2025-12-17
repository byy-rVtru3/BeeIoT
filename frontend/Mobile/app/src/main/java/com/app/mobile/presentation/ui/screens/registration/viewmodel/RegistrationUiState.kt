package com.app.mobile.presentation.ui.screens.registration.viewmodel

import com.app.mobile.presentation.models.account.RegistrationModelUi

sealed interface RegistrationUiState {
    data class Content(
        val registrationModelUi: RegistrationModelUi,
        val formState: RegistrationFormState = RegistrationFormState()
    ) : RegistrationUiState
    object Loading : RegistrationUiState
    data class Error(val message: String) : RegistrationUiState
}