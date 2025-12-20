package com.app.mobile.presentation.ui.screens.confirmation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.account.ConfirmationUserUseCase
import com.app.mobile.presentation.models.account.ConfirmationModelUi
import com.app.mobile.presentation.models.account.ConfirmationResultUi
import com.app.mobile.presentation.models.account.TypeConfirmationUi
import com.app.mobile.presentation.ui.screens.confirmation.ConfirmationRoute
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ConfirmationViewModel(
    savedStateHandle: SavedStateHandle,
    private val confirmationUserUseCase: ConfirmationUserUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ConfirmationRoute>()
    private val email = route.email
    private val type = route.type

    private val _confirmationUiState =
        MutableStateFlow<ConfirmationUiState>(ConfirmationUiState.Loading)
    val confirmationUiState = _confirmationUiState.asStateFlow()

    private val _navigationEvent = Channel<ConfirmationNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val handler = CoroutineExceptionHandler { _, exception ->
        _confirmationUiState.value = ConfirmationUiState.Error(exception.message ?: "Unknown error")
        Log.e("ConfirmationViewModel", exception.message.toString())
    }

    // Используем новый helper для валидации
    private val formValidator = ConfirmationFormValidator()

    // Job для управления таймером
    private var timerJob: Job? = null

    // Константа времени ожидания перед повторной отправкой (в секундах)
    private companion object {
        const val RESEND_TIMER_SECONDS = 10
    }

    fun onCodeChange(code: String) {
        val currentState = _confirmationUiState.value
        if (currentState is ConfirmationUiState.Content) {
            val validationResult = formValidator.validateCode(code)

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
            // Валидируем форму через helper - чисто и просто!
            val (validatedFormState, hasErrors) = formValidator.validateAndApply(currentState.formState)

            if (hasErrors) {
                _confirmationUiState.value = currentState.copy(formState = validatedFormState)
                Log.w("ConfirmationViewModel", "Form validation failed")
                return
            }

            _confirmationUiState.value = ConfirmationUiState.Loading

            val model = currentState.confirmationModelUi.copy(
                code = validatedFormState.code
            )

            viewModelScope.launch(handler) {
                val result = confirmationUserUseCase(
                    model.toDomain()
                ).toUiModel()

                when (result) {
                    is ConfirmationResultUi.Success -> {
                        _navigationEvent.send(ConfirmationNavigationEvent.NavigateToAuthorization)
                    }

                    is ConfirmationResultUi.Error -> {
                        _confirmationUiState.value = ConfirmationUiState.Error(result.message)
                    }
                }
            }
        }
    }

    fun createConfirmationModelUi() {
        val model = ConfirmationModelUi(email = email, code = "", type = type)
        val initialFormState = ConfirmationFormState(code = "")

        _confirmationUiState.value = ConfirmationUiState.Content(
            confirmationModelUi = model,
            formState = initialFormState,
            resendTimerSeconds = 0,
            canResendCode = true
        )
    }

    fun onResendCode() {
        val currentState = _confirmationUiState.value
        if (currentState is ConfirmationUiState.Content && currentState.canResendCode) {
            viewModelScope.launch(handler) {
                confirmationUserUseCase(
                    currentState.confirmationModelUi.toDomain()
                )

                Log.i("ConfirmationViewModel", "Resend code request sent")
            }

            startResendTimer()
        }
    }


    private fun startResendTimer() {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            val currentState = _confirmationUiState.value
            if (currentState is ConfirmationUiState.Content) {
                _confirmationUiState.value = currentState.copy(
                    resendTimerSeconds = RESEND_TIMER_SECONDS,
                    canResendCode = false
                )

                for (seconds in RESEND_TIMER_SECONDS - 1 downTo 0) {
                    delay(1000)

                    val state = _confirmationUiState.value
                    if (state is ConfirmationUiState.Content) {
                        _confirmationUiState.value = state.copy(
                            resendTimerSeconds = seconds
                        )
                    }
                }

                val finalState = _confirmationUiState.value
                if (finalState is ConfirmationUiState.Content) {
                    _confirmationUiState.value = finalState.copy(
                        resendTimerSeconds = 0,
                        canResendCode = true
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}