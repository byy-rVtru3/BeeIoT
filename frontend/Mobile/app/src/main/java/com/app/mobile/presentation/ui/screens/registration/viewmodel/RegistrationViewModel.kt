package com.app.mobile.presentation.ui.screens.registration.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.usecase.CreateUserAccountUseCase
import com.app.mobile.domain.usecase.RegistrationAccountUseCase
import com.app.mobile.presentation.mappers.toDomain
import com.app.mobile.presentation.mappers.toUiModel
import com.app.mobile.presentation.models.RegistrationModelUi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val registrationAccountUseCase: RegistrationAccountUseCase,
    private val createUserAccountUseCase: CreateUserAccountUseCase
) : ViewModel() {

    private val _registrationUiState = MutableLiveData<RegistrationUiState>()
    val registrationUiState: LiveData<RegistrationUiState> = _registrationUiState

    private val handler = CoroutineExceptionHandler { _, exception ->
        _registrationUiState.value = RegistrationUiState.Error(exception.message ?: "Unknown error")
        Log.e("RegistrationViewModel", exception.message.toString())
    }


    fun registrationAccount(registrationModelUi: RegistrationModelUi) {
        _registrationUiState.value = RegistrationUiState.Loading
        viewModelScope.launch(handler) {
            registrationAccountUseCase(registrationModelUi.toDomain())
        }
    }

    fun onEmailChange(email: String) {
        val currentState = _registrationUiState.value
        if (currentState is RegistrationUiState.Content) {
            val updatedModel = currentState.registrationModelUi.copy(email = email)
            _registrationUiState.value = RegistrationUiState.Content(updatedModel)
        }
    }

    fun onNameChange(name: String) {
        val currentState = _registrationUiState.value
        if (currentState is RegistrationUiState.Content) {
            val updatedModel = currentState.registrationModelUi.copy(name = name)
            _registrationUiState.value = RegistrationUiState.Content(updatedModel)
        }
    }

    fun onPasswordChange(password: String) {
        val currentState = _registrationUiState.value
        if (currentState is RegistrationUiState.Content) {
            val updatedModel = currentState.registrationModelUi.copy(password = password)
            _registrationUiState.value = RegistrationUiState.Content(updatedModel)
        }
    }

    fun onRepeatPasswordChange(repeatPassword: String) {
        val currentState = _registrationUiState.value
        if (currentState is RegistrationUiState.Content) {
            val updatedModel =
                currentState.registrationModelUi.copy(repeatPassword = repeatPassword)
            _registrationUiState.value = RegistrationUiState.Content(updatedModel)
        }
    }

    fun onRegisterClick() {
        TODO("Not yet implemented")
    }

    fun createUserAccount() {
        viewModelScope.launch(handler) {
            val user = createUserAccountUseCase().toUiModel()
            _registrationUiState.value = RegistrationUiState.Content(user)
        }
    }
}

