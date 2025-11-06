package com.app.mobile.presentation.ui.screens.confirmation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.ConfirmationUserUseCase
import com.app.mobile.presentation.models.ConfirmationModelUi
import com.app.mobile.presentation.models.ConfirmationResultUi
import com.app.mobile.presentation.models.TypeConfirmationUi
import com.app.mobile.presentation.validators.ConfirmationValidator
import com.app.mobile.presentation.validators.ValidationResult
import kotlinx.coroutines.launch

class ConfirmationViewModel(
    private val confirmationUserUseCase: ConfirmationUserUseCase
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
            val codeError =
                if (codeResult is ValidationResult.Error) codeResult.errors.firstOrNull() else null

            if (codeError != null) {
                val updatedModel = model.copy(codeError = codeError)
                _confirmationUiState.value = ConfirmationUiState.Content(updatedModel)
                return
            }

            viewModelScope.launch {
                val result = confirmationUserUseCase(
                    model.toDomain()
                ).toUiModel()

                when (result) {
                    is ConfirmationResultUi.Success -> {
                        TODO("Handle success state, e.g., navigate to next screen")
                    }

                    is ConfirmationResultUi.Error -> {
                        _confirmationUiState.value = ConfirmationUiState.Error(result.message)
                        TODO("Handle error state, e.g., show error message")
                    }
                }
            }
        }
    }

    fun createConfirmationModelUi(email: String, type: TypeConfirmationUi) {
        _confirmationUiState.value = ConfirmationUiState.Content(
            confirmationModelUi = ConfirmationModelUi(email = email, code = "", type = type)
        )
    }

    fun onResendCode() {
        val currentState = _confirmationUiState.value
        if (currentState is ConfirmationUiState.Content) {
            viewModelScope.launch {
                confirmationUserUseCase(
                    currentState.confirmationModelUi.toDomain()
                )
            }
        }
    }
}