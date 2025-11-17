package com.app.mobile.presentation.ui.screens.authorization.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.AuthorizationAccountUseCase
import com.app.mobile.presentation.models.AuthorizationModelUi
import com.app.mobile.presentation.models.AuthorizationResultUi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class AuthorizationViewModel(
    private val authorizationAccountUseCase: AuthorizationAccountUseCase
) : ViewModel() {
    private val _authorizationUiState = MutableLiveData<AuthorizationUiState>()
    val authorizationUiState: LiveData<AuthorizationUiState> = _authorizationUiState

    private val _navigationEvent = MutableLiveData<AuthorizationNavigationEvent?>()
    val navigationEvent: LiveData<AuthorizationNavigationEvent?> = _navigationEvent

    private val handler = CoroutineExceptionHandler { _, exception ->
        _authorizationUiState.value =
            AuthorizationUiState.Error(exception.message ?: "Unknown error")
        Log.e("RegistrationViewModel", exception.message.toString())
    }

    fun onAuthorizeClick() {
        val currentState = _authorizationUiState.value
        if (currentState is AuthorizationUiState.Content) {
            _authorizationUiState.value = AuthorizationUiState.Loading
            val model = currentState.authorizationModelUi
            viewModelScope.launch(handler) {
                when (val codeResult = authorizationAccountUseCase(model.toDomain()).toUiModel()) {
                    is AuthorizationResultUi.Success -> {
                        _navigationEvent.value =
                            AuthorizationNavigationEvent.NavigateToMainScreen
                    }

                    is AuthorizationResultUi.Error -> {
                        _authorizationUiState.value =
                            AuthorizationUiState.Error(codeResult.message)
                        // Добавить обработку ошибок

                    }
                }
            }

        }
    }

    fun onEmailChange(email: String) {
        val currentState = _authorizationUiState.value
        if (currentState is AuthorizationUiState.Content) {
            val updatedModel = currentState.authorizationModelUi.copy(
                email = email
            )
            _authorizationUiState.value = AuthorizationUiState.Content(updatedModel)
        }
    }

    fun onPasswordChange(password: String) {
        val currentState = _authorizationUiState.value
        if (currentState is AuthorizationUiState.Content) {
            val updatedModel = currentState.authorizationModelUi.copy(
                password = password
            )
            _authorizationUiState.value = AuthorizationUiState.Content(updatedModel)
        }
    }

    fun createAuthorizationModel() {
        _authorizationUiState.value = AuthorizationUiState.Content(AuthorizationModelUi("", ""))
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}