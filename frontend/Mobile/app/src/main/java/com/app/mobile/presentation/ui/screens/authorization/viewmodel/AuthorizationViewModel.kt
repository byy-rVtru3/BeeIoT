package com.app.mobile.presentation.ui.screens.authorization.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.AuthorizationAccountUseCase
import com.app.mobile.domain.usecase.ValidateAuthorizationFormUseCase
import com.app.mobile.presentation.models.AuthorizationModelUi
import com.app.mobile.presentation.models.AuthorizationResultUi
import com.app.mobile.presentation.validators.AuthorizationValidator
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class AuthorizationViewModel(
    private val authorizationAccountUseCase: AuthorizationAccountUseCase,
    private val validateFormUseCase: ValidateAuthorizationFormUseCase
) : ViewModel() {
    private val _authorizationUiState = MutableLiveData<AuthorizationUiState>()
    val authorizationUiState: LiveData<AuthorizationUiState> = _authorizationUiState

    private val _navigationEvent = MutableLiveData<AuthorizationNavigationEvent?>()
    val navigationEvent: LiveData<AuthorizationNavigationEvent?> = _navigationEvent

    private val validator = AuthorizationValidator()

    private val handler = CoroutineExceptionHandler { _, exception ->
        _authorizationUiState.value =
            AuthorizationUiState.Error(exception.message ?: "Unknown error")
        Log.e("AuthorizationViewModel", exception.message.toString())
    }

    fun onAuthorizeClick() {
        val currentState = _authorizationUiState.value
        if (currentState is AuthorizationUiState.Content) {
            _authorizationUiState.value = AuthorizationUiState.Loading

            val validatedFormState = validateFormUseCase(currentState.formState)

            if (validatedFormState.hasAnyError()) {
                _authorizationUiState.value = currentState.copy(formState = validatedFormState)
                Log.w("AuthorizationViewModel", "Form validation failed")
                return
            }

            val model = currentState.authorizationModelUi.copy(
                email = validatedFormState.email,
                password = validatedFormState.password
            )

            viewModelScope.launch(handler) {
                when (val result = authorizationAccountUseCase(model.toDomain()).toUiModel()) {
                    is AuthorizationResultUi.Success -> {
                        _navigationEvent.value =
                            AuthorizationNavigationEvent.NavigateToMainScreen
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
            val validationResult = validator.validateEmail(email)

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
            val validationResult = validator.validatePassword(password)

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
            _navigationEvent.value = AuthorizationNavigationEvent.NavigateToRegistration
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}