package com.app.mobile.presentation.ui.screens.confirmation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.account.ConfirmationUserUseCase
import com.app.mobile.presentation.models.account.ConfirmationModelUi
import com.app.mobile.presentation.models.account.ConfirmationResultUi
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.confirmation.ConfirmationRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConfirmationViewModel(
    savedStateHandle: SavedStateHandle,
    private val confirmationUserUseCase: ConfirmationUserUseCase
) : BaseViewModel<ConfirmationUiState, ConfirmationNavigationEvent>(ConfirmationUiState.Loading) {

    private val route = savedStateHandle.toRoute<ConfirmationRoute>()
    private val email = route.email
    private val type = route.type

    override fun handleError(exception: Throwable) {
        updateState { ConfirmationUiState.Error(exception.message ?: "Unknown error") }
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
        val state = currentState
        if (state is ConfirmationUiState.Content) {
            val validationResult = formValidator.validateCode(code)

            val updatedFormState = state.formState.copy(
                code = validationResult.data,
                codeError = null
            )

            updateState { state.copy(formState = updatedFormState) }
        }
    }

    fun onConfirmClick() {
        val state = currentState
        if (state is ConfirmationUiState.Content) {
            // Валидируем форму через helper - чисто и просто!
            val (validatedFormState, hasErrors) = formValidator.validateAndApply(state.formState)

            if (hasErrors) {
                updateState { state.copy(formState = validatedFormState) }
                Log.w("ConfirmationViewModel", "Form validation failed")
                return
            }

            updateState { ConfirmationUiState.Loading }

            val model = state.confirmationModelUi.copy(
                code = validatedFormState.code
            )

            launch {
                val result = confirmationUserUseCase(
                    model.toDomain()
                ).toUiModel()

                when (result) {
                    is ConfirmationResultUi.Success -> {
                        sendEvent(ConfirmationNavigationEvent.NavigateToAuthorization)
                    }

                    is ConfirmationResultUi.Error -> {
                        updateState { ConfirmationUiState.Error(result.message) }
                    }
                }
            }
        }
    }

    fun createConfirmationModelUi() {
        val model = ConfirmationModelUi(email = email, code = "", type = type)
        val initialFormState = ConfirmationFormState(code = "")

        updateState {
            ConfirmationUiState.Content(
                confirmationModelUi = model,
                formState = initialFormState,
                resendTimerSeconds = 0,
                canResendCode = true
            )
        }
    }

    fun onResendCode() {
        val state = currentState
        if (state is ConfirmationUiState.Content && state.canResendCode) {
            launch {
                confirmationUserUseCase(
                    state.confirmationModelUi.toDomain()
                )

                Log.i("ConfirmationViewModel", "Resend code request sent")
            }

            startResendTimer()
        }
    }


    private fun startResendTimer() {
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            val state = currentState
            if (state is ConfirmationUiState.Content) {
                updateState {
                    state.copy(
                        resendTimerSeconds = RESEND_TIMER_SECONDS,
                        canResendCode = false
                    )
                }

                for (seconds in RESEND_TIMER_SECONDS - 1 downTo 0) {
                    delay(1000)

                    val state = currentState
                    if (state is ConfirmationUiState.Content) {
                        updateState {
                            state.copy(
                                resendTimerSeconds = seconds
                            )
                        }
                    }
                }

                val finalState = currentState
                if (finalState is ConfirmationUiState.Content) {
                    updateState {
                        finalState.copy(
                            resendTimerSeconds = 0,
                            canResendCode = true
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}