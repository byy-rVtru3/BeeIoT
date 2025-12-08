package com.app.mobile.presentation.ui.screens.registration.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.CreateUserAccountUseCase
import com.app.mobile.domain.usecase.RegistrationAccountUseCase
import com.app.mobile.domain.usecase.ValidateRegistrationFormUseCase
import com.app.mobile.presentation.mappers.toDomain
import com.app.mobile.presentation.models.RegistrationResultUi
import com.app.mobile.presentation.models.TypeConfirmationUi
import com.app.mobile.presentation.validators.RegistrationValidator
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val registrationAccountUseCase: RegistrationAccountUseCase,
    private val createUserAccountUseCase: CreateUserAccountUseCase,
    private val validateFormUseCase: ValidateRegistrationFormUseCase
) : ViewModel() {

    private val _registrationUiState = MutableLiveData<RegistrationUiState>()
    val registrationUiState: LiveData<RegistrationUiState> = _registrationUiState

    private val _navigationEvent = MutableLiveData<RegistrationNavigationEvent?>()
    val navigationEvent: LiveData<RegistrationNavigationEvent?> = _navigationEvent

    private val validator = RegistrationValidator()

    private val handler = CoroutineExceptionHandler { _, exception ->
        _registrationUiState.value = RegistrationUiState.Error(exception.message ?: "Unknown error")
        Log.e("RegistrationViewModel", exception.message.toString())
    }

    fun onEmailChange(email: String) {
        val currentState = _registrationUiState.value
        if (currentState is RegistrationUiState.Content) {
            val validationResult = validator.validateEmail(email)

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
            val validationResult = validator.validateName(name)

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
            val validationResult = validator.validatePassword(password)

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
            val validationResult = validator.validateRepeatPassword(
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
            val validatedFormState = validateFormUseCase(currentState.formState)

            if (validatedFormState.hasAnyError()) {
                _registrationUiState.value = currentState.copy(formState = validatedFormState)
                Log.w("RegistrationViewModel", "Form validation failed")
                return
            }

            _registrationUiState.value = RegistrationUiState.Loading

            // Создаем модель из формы для отправки
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
                        _navigationEvent.value = RegistrationNavigationEvent.NavigateToConfirmation(
                            email = validatedFormState.email,
                            type = TypeConfirmationUi.REGISTRATION
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

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}
