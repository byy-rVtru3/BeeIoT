package com.app.mobile.presentation.ui.screens.registration.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.CreateUserAccountUseCase
import com.app.mobile.domain.usecase.RegistrationAccountUseCase
import com.app.mobile.presentation.mappers.toDomain
import com.app.mobile.presentation.models.RegistrationResultUi
import com.app.mobile.presentation.models.TypeConfirmationUi
import com.app.mobile.presentation.validators.RegistrationValidator
import com.app.mobile.presentation.validators.ValidationResult
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val registrationAccountUseCase: RegistrationAccountUseCase,
    private val createUserAccountUseCase: CreateUserAccountUseCase
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

            val updatedModel = currentState.registrationModelUi.copy(
                email = validationResult.data,
                emailError = null
            )
            _registrationUiState.value = RegistrationUiState.Content(updatedModel)
        }
    }

    fun onNameChange(name: String) {
        val currentState = _registrationUiState.value
        if (currentState is RegistrationUiState.Content) {
            val validationResult = validator.validateName(name)

            val updatedModel = currentState.registrationModelUi.copy(
                name = validationResult.data,
                nameError = null
            )
            _registrationUiState.value = RegistrationUiState.Content(updatedModel)
        }
    }

    fun onPasswordChange(password: String) {
        val currentState = _registrationUiState.value
        if (currentState is RegistrationUiState.Content) {
            val validationResult = validator.validatePassword(password)

            val updatedModel = currentState.registrationModelUi.copy(
                password = validationResult.data,
                passwordError = null
            )
            _registrationUiState.value = RegistrationUiState.Content(updatedModel)
        }
    }

    fun onRepeatPasswordChange(repeatPassword: String) {
        val currentState = _registrationUiState.value
        if (currentState is RegistrationUiState.Content) {
            val validationResult = validator.validateRepeatPassword(
                currentState.registrationModelUi.password,
                repeatPassword
            )

            val updatedModel = currentState.registrationModelUi.copy(
                repeatPassword = validationResult.data,
                repeatPasswordError = null
            )
            _registrationUiState.value = RegistrationUiState.Content(updatedModel)
        }
    }

    fun onRegisterClick() {
        val currentState = _registrationUiState.value
        if (currentState is RegistrationUiState.Content) {
            _registrationUiState.value = RegistrationUiState.Loading
            val model = currentState.registrationModelUi

            val nameResult = validator.validateName(model.name)
            val emailResult = validator.validateEmail(model.email)
            val passwordResult = validator.validatePassword(model.password)
            val repeatPasswordResult =
                validator.validateRepeatPassword(model.password, model.repeatPassword)

            val nameError =
                if (nameResult is ValidationResult.Error) nameResult.errors.firstOrNull() else null
            val emailError =
                if (emailResult is ValidationResult.Error) emailResult.errors.firstOrNull() else null
            val passwordError =
                if (passwordResult is ValidationResult.Error) passwordResult.errors.firstOrNull() else null
            val repeatPasswordError =
                if (repeatPasswordResult is ValidationResult.Error) repeatPasswordResult.errors.firstOrNull() else null

            if (nameError != null || emailError != null || passwordError != null || repeatPasswordError != null) {
                val updatedModel = model.copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    repeatPasswordError = repeatPasswordError
                )
                _registrationUiState.value = RegistrationUiState.Content(updatedModel)
                Log.w("RegistrationViewModel", "Form validation failed")
                return
            }

            viewModelScope.launch(handler) {
                val response = registrationAccountUseCase(
                    currentState.registrationModelUi.toDomain()
                ).toUiModel()

                when (response) {
                    is RegistrationResultUi.Success -> {
                        _navigationEvent.value = RegistrationNavigationEvent.NavigateToConfirmation(
                            email = currentState.registrationModelUi.email,
                            type = TypeConfirmationUi.REGISTRATION
                        )
                    }

                    is RegistrationResultUi.Error -> {
                        _registrationUiState.value = RegistrationUiState.Error(response.message)
                        // Добавить обработку ошибок
                    }
                }
            }
        }
    }

    fun createUserAccount() {
        viewModelScope.launch(handler) {
            val user = createUserAccountUseCase().toUiModel()
            _registrationUiState.value = RegistrationUiState.Content(user)
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}
