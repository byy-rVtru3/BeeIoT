package com.app.mobile.presentation.ui.screens.authorization.viewmodel

import android.util.Log
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.account.AuthorizationAccountUseCase
import com.app.mobile.presentation.models.account.AuthorizationModelUi
import com.app.mobile.presentation.models.account.AuthorizationResultUi
import com.app.mobile.presentation.ui.components.BaseViewModel

class AuthorizationViewModel(
    private val authorizationAccountUseCase: AuthorizationAccountUseCase
) : BaseViewModel<AuthorizationUiState, AuthorizationNavigationEvent>(AuthorizationUiState.Loading) {

    override fun handleError(exception: Throwable) {
        updateState { AuthorizationUiState.Error(exception.message ?: "Unknown error") }
        Log.e("AuthorizationViewModel", exception.message.toString())
    }

    // Используем новый helper для валидации
    private val formValidator = AuthorizationFormValidator()

    fun onAuthorizeClick() {
        val state = currentState
        if (state is AuthorizationUiState.Content) {
            // Валидируем форму через helper - чисто и просто!
            val (validatedFormState, hasErrors) = formValidator.validateAndApply(state.formState)

            if (hasErrors) {
                updateState { state.copy(formState = validatedFormState) }
                Log.w("AuthorizationViewModel", "Form validation failed")
                return
            }

            updateState { AuthorizationUiState.Loading }

            val model = state.authorizationModelUi.copy(
                email = validatedFormState.email,
                password = validatedFormState.password
            )

            launch {
                when (val result = authorizationAccountUseCase(model.toDomain()).toUiModel()) {
                    is AuthorizationResultUi.Success -> {
                        sendEvent(
                            AuthorizationNavigationEvent.NavigateToMainScreen
                        )
                    }

                    is AuthorizationResultUi.Error -> {
                        updateState { AuthorizationUiState.Error(result.message) }
                    }
                }
            }
        }
    }

    fun onEmailChange(email: String) {
        val state = currentState
        if (state is AuthorizationUiState.Content) {
            val validationResult = formValidator.validateEmail(email)

            val updatedFormState = state.formState.copy(
                email = validationResult.data,
                emailError = null
            )

            val updatedModel = state.authorizationModelUi.copy(
                email = validationResult.data
            )

            updateState {
                state.copy(
                    authorizationModelUi = updatedModel,
                    formState = updatedFormState
                )
            }
        }
    }

    fun onPasswordChange(password: String) {
        val state = currentState
        if (state is AuthorizationUiState.Content) {
            val validationResult = formValidator.validatePassword(password)

            val updatedFormState = state.formState.copy(
                password = validationResult.data,
                passwordError = null
            )

            val updatedModel = state.authorizationModelUi.copy(
                password = validationResult.data
            )

            updateState {
                state.copy(
                    authorizationModelUi = updatedModel,
                    formState = updatedFormState
                )
            }
        }
    }

    fun createAuthorizationModel() {
        updateState {
            AuthorizationUiState.Content(
                authorizationModelUi = AuthorizationModelUi("", ""),
                formState = AuthorizationFormState()
            )
        }
    }

    fun onRegistrationClick() {
        val state = currentState
        if (state is AuthorizationUiState.Content) {
            launch {
                sendEvent(AuthorizationNavigationEvent.NavigateToRegistration)
            }
        }
    }
}