package com.app.mobile.presentation.ui.screens.authorization.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.account.AuthorizationAccountUseCase
import com.app.mobile.presentation.models.account.AuthorizationModelUi
import com.app.mobile.presentation.models.account.AuthorizationResultUi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AuthorizationViewModel(
    private val authorizationAccountUseCase: AuthorizationAccountUseCase
) : ViewModel() {

    private val _authorizationUiState =
        MutableStateFlow<AuthorizationUiState>(AuthorizationUiState.Loading)
    val authorizationUiState = _authorizationUiState.asStateFlow()

    private val _navigationEvent = Channel<AuthorizationNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    // Используем новый helper для валидации
    private val formValidator = AuthorizationFormValidator()

    private val handler = CoroutineExceptionHandler { _, exception ->
        _authorizationUiState.value =
            AuthorizationUiState.Error(exception.message ?: "Unknown error")
        Log.e("AuthorizationViewModel", exception.message.toString())
    }

    fun onAuthorizeClick() {
        val currentState = _authorizationUiState.value
        if (currentState is AuthorizationUiState.Content) {
            // Валидируем форму через helper - чисто и просто!
            val (validatedFormState, hasErrors) = formValidator.validateAndApply(currentState.formState)

            if (hasErrors) {
                _authorizationUiState.value = currentState.copy(formState = validatedFormState)
                Log.w("AuthorizationViewModel", "Form validation failed")
                return
            }

            _authorizationUiState.value = AuthorizationUiState.Loading

            val model = currentState.authorizationModelUi.copy(
                email = validatedFormState.email,
                password = validatedFormState.password
            )

            viewModelScope.launch(handler) {
                when (val result = authorizationAccountUseCase(model.toDomain()).toUiModel()) {
                    is AuthorizationResultUi.Success -> {
                        _navigationEvent.send(
                            AuthorizationNavigationEvent.NavigateToMainScreen
                        )
                    }

                    is AuthorizationResultUi.Error -> {
                        _authorizationUiState.value =
                            AuthorizationUiState.Error(result.message)
                    }
                }
            }
        }
    }

    fun onEmailChange(email: String) {
        val currentState = _authorizationUiState.value
        if (currentState is AuthorizationUiState.Content) {
            val validationResult = formValidator.validateEmail(email)

            val updatedFormState = currentState.formState.copy(
                email = validationResult.data,
                emailError = null
            )

            val updatedModel = currentState.authorizationModelUi.copy(
                email = validationResult.data
            )

            _authorizationUiState.value = currentState.copy(
                authorizationModelUi = updatedModel,
                formState = updatedFormState
            )
        }
    }

    fun onPasswordChange(password: String) {
        val currentState = _authorizationUiState.value
        if (currentState is AuthorizationUiState.Content) {
            val validationResult = formValidator.validatePassword(password)

            val updatedFormState = currentState.formState.copy(
                password = validationResult.data,
                passwordError = null
            )

            val updatedModel = currentState.authorizationModelUi.copy(
                password = validationResult.data
            )

            _authorizationUiState.value = currentState.copy(
                authorizationModelUi = updatedModel,
                formState = updatedFormState
            )
        }
    }

    fun createAuthorizationModel() {
        _authorizationUiState.value = AuthorizationUiState.Content(
            authorizationModelUi = AuthorizationModelUi("", ""),
            formState = AuthorizationFormState()
        )
    }

    fun onRegistrationClick() {
        val currentState = _authorizationUiState.value
        if (currentState is AuthorizationUiState.Content) {
            viewModelScope.launch(handler) {
                _navigationEvent.send(AuthorizationNavigationEvent.NavigateToRegistration)
            }
        }
    }
}