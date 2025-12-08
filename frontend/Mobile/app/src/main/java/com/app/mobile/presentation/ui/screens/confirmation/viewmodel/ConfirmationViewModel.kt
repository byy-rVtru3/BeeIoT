package com.app.mobile.presentation.ui.screens.confirmation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.ConfirmationUserUseCase
import com.app.mobile.domain.usecase.ValidateConfirmationFormUseCase
import com.app.mobile.presentation.models.ConfirmationModelUi
import com.app.mobile.presentation.models.ConfirmationResultUi
import com.app.mobile.presentation.models.TypeConfirmationUi
import com.app.mobile.presentation.validators.ConfirmationValidator
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class ConfirmationViewModel(
    private val confirmationUserUseCase: ConfirmationUserUseCase,
    private val validateFormUseCase: ValidateConfirmationFormUseCase
) : ViewModel() {

    private val _confirmationUiState = MutableLiveData<ConfirmationUiState>()
    val confirmationUiState: LiveData<ConfirmationUiState> = _confirmationUiState

    private val _navigationEvent = MutableLiveData<ConfirmationNavigationEvent?>()
    val navigationEvent: LiveData<ConfirmationNavigationEvent?> = _navigationEvent

    private val handler = CoroutineExceptionHandler { _, exception ->
        _confirmationUiState.value = ConfirmationUiState.Error(exception.message ?: "Unknown error")
        Log.e("ConfirmationViewModel", exception.message.toString())
    }

    private val validator = ConfirmationValidator()

    fun onCodeChange(code: String) {
        val currentState = _confirmationUiState.value
        if (currentState is ConfirmationUiState.Content) {
            val validationResult = validator.validateCode(code)

            // Обновляем formState
            val updatedFormState = currentState.formState.copy(
                code = validationResult.data,
                codeError = null
            )

            _confirmationUiState.value = currentState.copy(formState = updatedFormState)
        }
    }

    fun onConfirmClick() {
        val currentState = _confirmationUiState.value
        if (currentState is ConfirmationUiState.Content) {
            val validatedFormState = validateFormUseCase(currentState.formState)

            if (validatedFormState.hasAnyError()) {
                _confirmationUiState.value = currentState.copy(formState = validatedFormState)
                Log.w("ConfirmationViewModel", "Form validation failed")
                return
            }

            _confirmationUiState.value = ConfirmationUiState.Loading

            // Создаем модель из формы для отправки
            val model = currentState.confirmationModelUi.copy(
                code = validatedFormState.code
            )

            viewModelScope.launch(handler) {
                val result = confirmationUserUseCase(
                    model.toDomain()
                ).toUiModel()

                when (result) {
                    is ConfirmationResultUi.Success -> {
                        _navigationEvent.value = ConfirmationNavigationEvent.NavigateToAuthorization
                    }

                    is ConfirmationResultUi.Error -> {
                        _confirmationUiState.value = ConfirmationUiState.Error(result.message)
                    }
                }
            }
        }
    }

    fun createConfirmationModelUi(email: String, type: TypeConfirmationUi) {
        val model = ConfirmationModelUi(email = email, code = "", type = type)
        val initialFormState = ConfirmationFormState(code = "")

        _confirmationUiState.value = ConfirmationUiState.Content(
            confirmationModelUi = model,
            formState = initialFormState
        )
    }

    fun onResendCode() {
        val currentState = _confirmationUiState.value
        if (currentState is ConfirmationUiState.Content) {
            viewModelScope.launch(handler) {
                confirmationUserUseCase(
                    currentState.confirmationModelUi.toDomain()
                )
            }
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}