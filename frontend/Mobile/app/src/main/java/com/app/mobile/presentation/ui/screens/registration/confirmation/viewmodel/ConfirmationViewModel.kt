package com.app.mobile.presentation.ui.screens.registration.confirmation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.usecase.ConfirmationUserUseCase
import kotlinx.coroutines.launch

class ConfirmationViewModel(
    private val confirmationUserUseCase: ConfirmationUserUseCase
) : ViewModel() {

    private val _confirmationUiState = MutableLiveData<ConfirmationUiState>()
    val confirmationUiState: LiveData<ConfirmationUiState> = _confirmationUiState

    fun onCodeChange(code: String) {
        val currentState = _confirmationUiState.value
        if (currentState is ConfirmationUiState.Content) {
            val updatedModel = currentState.confirmationModelUi.copy(code = code)
            _confirmationUiState.value = ConfirmationUiState.Content(updatedModel)
        }
    }

    fun confirmUser() {
        val currentState = _confirmationUiState.value
        if (currentState is ConfirmationUiState.Content) {
            viewModelScope.launch {
                val result = confirmationUserUseCase(currentState.confirmationModelUi.userId,
                    currentState.confirmationModelUi.code)
                if (result) {
                    TODO("Navigate to next screen")
                } else {
                    TODO("output if not confirmed")
                }
            }
        }
    }
}