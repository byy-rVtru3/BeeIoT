package com.app.mobile.presentation.ui.screens.registration.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.account.CreateUserAccountUseCase
import com.app.mobile.domain.usecase.account.RegistrationAccountUseCase
import com.app.mobile.presentation.mappers.toDomain
import com.app.mobile.presentation.models.account.RegistrationResultUi
import com.app.mobile.presentation.models.account.TypeConfirmationUi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val registrationAccountUseCase: RegistrationAccountUseCase,
    private val createUserAccountUseCase: CreateUserAccountUseCase
) : ViewModel() {

    private val _registrationUiState =
        MutableStateFlow<RegistrationUiState>(RegistrationUiState.Loading)
    val registrationUiState = _registrationUiState.asStateFlow()

    private val _navigationEvent = Channel<RegistrationNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    // Используем новый helper для валидации
    private val formValidator = RegistrationFormValidator()

    private val handler = CoroutineExceptionHandler { _, exception ->
        _registrationUiState.value = RegistrationUiState.Error(exception.message ?: "Unknown error")
        Log.e("RegistrationViewModel", exception.message.toString())
    }

    fun onEmailChange(email: String) {
        val currentState = _registrationUiState.value
        if (currentState is RegistrationUiState.Content) {
            val validationResult = formValidator.validateEmail(email)

            val updatedFormState = currentState.formState.copy(
                email = validationResult.data,
                emailError = null
            )

            _registrationUiState.value = currentState.copy(formState = updatedFormState)
        }
    }

    fun onNameChange(name: String) {
        val currentState = _registrationUiState.value
        if (currentState is RegistrationUiState.Content) {
            val validationResult = formValidator.validateName(name)

            val updatedFormState = currentState.formState.copy(
                name = validationResult.data,
                nameError = null
            )

            _registrationUiState.value = currentState.copy(formState = updatedFormState)
        }
    }

    fun onPasswordChange(password: String) {
        val currentState = _registrationUiState.value
        if (currentState is RegistrationUiState.Content) {
            val validationResult = formValidator.validatePassword(password)

            val updatedFormState = currentState.formState.copy(
                password = validationResult.data,
                passwordError = null
            )

            _registrationUiState.value = currentState.copy(formState = updatedFormState)
        }
    }

    fun onRepeatPasswordChange(repeatPassword: String) {
        val currentState = _registrationUiState.value
        if (currentState is RegistrationUiState.Content) {
            val validationResult = formValidator.validateRepeatPassword(
                currentState.formState.password,
                repeatPassword
            )

            val updatedFormState = currentState.formState.copy(
                repeatPassword = validationResult.data,
                repeatPasswordError = null
            )

            _registrationUiState.value = currentState.copy(formState = updatedFormState)
        }
    }

    fun onRegisterClick() {
        val currentState = _registrationUiState.value
        if (currentState is RegistrationUiState.Content) {
            // Валидируем форму через helper - чисто и просто!
            val (validatedFormState, hasErrors) = formValidator.validateAndApply(currentState.formState)

            if (hasErrors) {
                // Применяем ошибки к состоянию
                _registrationUiState.value = currentState.copy(formState = validatedFormState)
                Log.w("RegistrationViewModel", "Form validation failed")
                return
            }

            // Валидация прошла успешно - показываем Loading
            _registrationUiState.value = RegistrationUiState.Loading

            // Создаем модель для отправки из валидированной формы
            val registrationModel = currentState.registrationModelUi.copy(
                name = validatedFormState.name,
                email = validatedFormState.email,
                password = validatedFormState.password,
                repeatPassword = validatedFormState.repeatPassword
            )

            viewModelScope.launch(handler) {
                val response = registrationAccountUseCase(
                    registrationModel.toDomain()
                ).toUiModel()

                when (response) {
                    is RegistrationResultUi.Success -> {
                        _navigationEvent.send(
                            RegistrationNavigationEvent.NavigateToConfirmation(
                                email = validatedFormState.email,
                                type = TypeConfirmationUi.REGISTRATION
                            )
                        )
                    }

                    is RegistrationResultUi.Error -> {
                        _registrationUiState.value = RegistrationUiState.Error(response.message)
                    }
                }
            }
        }
    }

    fun createUserAccount() {
        viewModelScope.launch(handler) {
            val user = createUserAccountUseCase().toUiModel()

            val initialFormState = RegistrationFormState(
                name = user.name,
                email = user.email,
                password = user.password,
                repeatPassword = user.repeatPassword
            )

            _registrationUiState.value = RegistrationUiState.Content(
                registrationModelUi = user,
                formState = initialFormState
            )
        }
    }
}
