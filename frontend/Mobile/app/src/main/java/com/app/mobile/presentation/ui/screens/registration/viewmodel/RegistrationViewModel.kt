package com.app.mobile.presentation.ui.screens.registration.viewmodel

import android.util.Log
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.account.CreateUserAccountUseCase
import com.app.mobile.domain.usecase.account.RegistrationAccountUseCase
import com.app.mobile.presentation.mappers.toDomain
import com.app.mobile.presentation.models.account.RegistrationResultUi
import com.app.mobile.presentation.models.account.TypeConfirmationUi
import com.app.mobile.presentation.ui.components.BaseViewModel

class RegistrationViewModel(
    private val registrationAccountUseCase: RegistrationAccountUseCase,
    private val createUserAccountUseCase: CreateUserAccountUseCase
) : BaseViewModel<RegistrationUiState, RegistrationNavigationEvent>(RegistrationUiState.Loading) {

    override fun handleError(exception: Throwable) {
        updateState { RegistrationUiState.Error(exception.message ?: "Unknown error") }
        Log.e("RegistrationViewModel", exception.message.toString())
    }

    // Используем новый helper для валидации
    private val formValidator = RegistrationFormValidator()

    fun onEmailChange(email: String) {
        val state = currentState
        if (state is RegistrationUiState.Content) {
            val validationResult = formValidator.validateEmail(email)

            val updatedFormState = state.formState.copy(
                email = validationResult.data,
                emailError = null
            )

            updateState { state.copy(formState = updatedFormState) }
        }
    }

    fun onNameChange(name: String) {
        val state = currentState
        if (state is RegistrationUiState.Content) {
            val validationResult = formValidator.validateName(name)

            val updatedFormState = state.formState.copy(
                name = validationResult.data,
                nameError = null
            )

            updateState { state.copy(formState = updatedFormState) }
        }
    }

    fun onPasswordChange(password: String) {
        val state = currentState
        if (state is RegistrationUiState.Content) {
            val validationResult = formValidator.validatePassword(password)

            val updatedFormState = state.formState.copy(
                password = validationResult.data,
                passwordError = null
            )

            updateState { state.copy(formState = updatedFormState) }
        }
    }

    fun onRepeatPasswordChange(repeatPassword: String) {
        val state = currentState
        if (state is RegistrationUiState.Content) {
            val validationResult = formValidator.validateRepeatPassword(
                state.formState.password,
                repeatPassword
            )

            val updatedFormState = state.formState.copy(
                repeatPassword = validationResult.data,
                repeatPasswordError = null
            )

            updateState { state.copy(formState = updatedFormState) }
        }
    }

    fun onRegisterClick() {
        val state = currentState
        if (state is RegistrationUiState.Content) {
            // Валидируем форму через helper - чисто и просто!
            val (validatedFormState, hasErrors) = formValidator.validateAndApply(state.formState)

            if (hasErrors) {
                // Применяем ошибки к состоянию
                updateState { state.copy(formState = validatedFormState) }
                Log.w("RegistrationViewModel", "Form validation failed")
                return
            }

            // Валидация прошла успешно - показываем Loading
            updateState { RegistrationUiState.Loading }

            // Создаем модель для отправки из валидированной формы
            val registrationModel = state.registrationModelUi.copy(
                name = validatedFormState.name,
                email = validatedFormState.email,
                password = validatedFormState.password,
                repeatPassword = validatedFormState.repeatPassword
            )

            launch {
                val response = registrationAccountUseCase(
                    registrationModel.toDomain()
                ).toUiModel()

                when (response) {
                    is RegistrationResultUi.Success -> {
                        sendEvent(
                            RegistrationNavigationEvent.NavigateToConfirmation(
                                email = validatedFormState.email,
                                type = TypeConfirmationUi.REGISTRATION
                            )
                        )
                    }

                    is RegistrationResultUi.Error -> {
                        updateState { RegistrationUiState.Error(response.message) }
                    }
                }
            }
        }
    }

    fun createUserAccount() {
        launch {
            val user = createUserAccountUseCase().toUiModel()

            val initialFormState = RegistrationFormState(
                name = user.name,
                email = user.email,
                password = user.password,
                repeatPassword = user.repeatPassword
            )

            updateState {
                RegistrationUiState.Content(
                    registrationModelUi = user,
                    formState = initialFormState
                )
            }
        }
    }
}
