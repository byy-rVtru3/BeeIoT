package com.app.mobile.presentation.ui.screens.confirmation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.usecase.ConfirmationUserUseCase
import com.app.mobile.domain.usecase.ResendConfirmationCodeUseCase
import com.app.mobile.presentation.models.ConfirmationModelUi
import kotlinx.coroutines.launch

class ConfirmationViewModel(
    private val confirmationUserUseCase: ConfirmationUserUseCase,
    private val resendConfirmationCodeUseCase: ResendConfirmationCodeUseCase
) : ViewModel() {

    private val _confirmationUiState = MutableLiveData<ConfirmationUiState>()
    val confirmationUiState: LiveData<ConfirmationUiState> = _confirmationUiState

    fun onCodeChange(code: String) {
        val currentState = _confirmationUiState.value
        if (currentState is ConfirmationUiState.Content) {
            val updatedModel = currentState.confirmationModelUi.copy(code = code)
            _confirmationUiState.value = ConfirmationUiState.Content(updatedModel)
        }
    }

    fun onConfirmClick() {
        val currentState = _confirmationUiState.value
        if (currentState is ConfirmationUiState.Content) {
            viewModelScope.launch {
                val result = confirmationUserUseCase(currentState.confirmationModelUi.email,
                    currentState.confirmationModelUi.code, currentState.confirmationModelUi.type)
                if (result) {
                    TODO("Navigate to next screen")
                } else {
                    TODO("output if not confirmed")
                }
            }
        }
    }

    fun createConfirmationModelUi(email: String, type: String) {
        _confirmationUiState.value = ConfirmationUiState.Content(
            confirmationModelUi = ConfirmationModelUi(email = email, code = "", type = type)
        )
    }

    fun onResendCode() {
        val currentState = _confirmationUiState.value
        if (currentState is ConfirmationUiState.Content) {
            viewModelScope.launch {
                resendConfirmationCodeUseCase(currentState.confirmationModelUi.email,
                    currentState.confirmationModelUi.type)
            }
        }
    }
}
