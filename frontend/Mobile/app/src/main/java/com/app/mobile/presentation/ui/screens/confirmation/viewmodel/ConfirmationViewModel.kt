package com.app.mobile.presentation.ui.screens.confirmation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.usecase.ConfirmationUserUseCase
import com.app.mobile.domain.usecase.ResendConfirmationCodeUseCase
import com.app.mobile.presentation.models.ConfirmationModelUi
import com.app.mobile.presentation.validators.ConfirmationValidator
import com.app.mobile.presentation.validators.ValidationResult
import kotlinx.coroutines.launch

class ConfirmationViewModel(
    private val confirmationUserUseCase: ConfirmationUserUseCase,
    private val resendConfirmationCodeUseCase: ResendConfirmationCodeUseCase
) : ViewModel() {

    private val _confirmationUiState = MutableLiveData<ConfirmationUiState>()
    val confirmationUiState: LiveData<ConfirmationUiState> = _confirmationUiState

    private val validator = ConfirmationValidator()

    fun onCodeChange(code: String) {
        val currentState = _confirmationUiState.value
        if (currentState is ConfirmationUiState.Content) {
            val validationResult = validator.validateCode(code)

            val updatedModel = currentState.confirmationModelUi.copy(
                code = validationResult.data,
                codeError = null
            )
            _confirmationUiState.value = ConfirmationUiState.Content(updatedModel)
        }
    }

    fun onConfirmClick() {
        val currentState = _confirmationUiState.value
        if (currentState is ConfirmationUiState.Content) {
            val model = currentState.confirmationModelUi

            val codeResult = validator.validateCode(model.code)
            val codeError = if (codeResult is ValidationResult.Error) codeResult.errors.firstOrNull() else null

            if (codeError != null) {
                val updatedModel = model.copy(codeError = codeError)
                _confirmationUiState.value = ConfirmationUiState.Content(updatedModel)
                return
            }

            viewModelScope.launch {
                val result = confirmationUserUseCase(
                    currentState.confirmationModelUi.email,
                    currentState.confirmationModelUi.code,
                    currentState.confirmationModelUi.type
                )
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
                resendConfirmationCodeUseCase(
                    currentState.confirmationModelUi.email,
                    currentState.confirmationModelUi.type
                )
            }
        }
    }
}
