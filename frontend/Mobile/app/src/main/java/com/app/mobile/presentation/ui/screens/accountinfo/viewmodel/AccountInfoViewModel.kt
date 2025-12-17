package com.app.mobile.presentation.ui.screens.accountinfo.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toPresentation
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.account.DeleteAccountUseCase
import com.app.mobile.domain.usecase.account.GetAccountInfoUseCase
import com.app.mobile.presentation.models.account.DeleteResultUi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class AccountInfoViewModel(
    private val getAccountInfoUseCase: GetAccountInfoUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _accountInfoUiState = MutableLiveData<AccountInfoUiState>()
    val accountInfoUiState: LiveData<AccountInfoUiState> = _accountInfoUiState

    private val _accountInfoDialogState = MutableLiveData<AccountInfoDialogState>()
    val accountInfoDialogState: LiveData<AccountInfoDialogState> = _accountInfoDialogState

    private val _navigationEvent = MutableLiveData<AccountInfoNavigationEvent?>()
    val navigationEvent: LiveData<AccountInfoNavigationEvent?> = _navigationEvent

    val handler = CoroutineExceptionHandler { _, exception ->
        _accountInfoUiState.value = AccountInfoUiState.Error(exception.message.toString())
        Log.e("AccountInfoViewModel", exception.message.toString())
    }

    fun getAccountInfo() {
        _accountInfoUiState.value = AccountInfoUiState.Loading
        viewModelScope.launch(handler) {
            val user = getAccountInfoUseCase()?.toPresentation()
            if (user != null) {
                _accountInfoUiState.value = AccountInfoUiState.Content(user)
            } else {
                _accountInfoUiState.value = AccountInfoUiState.Error("Пользователь не найден")
            }
        }
    }

    fun onNameClick() {
        val currentState = _accountInfoUiState.value
        if (currentState is AccountInfoUiState.Content) {
            viewModelScope.launch(handler) {
                _accountInfoDialogState.value = AccountInfoDialogState.SetName(
                    currentState.userInfo.name
                )

            }
        }
    }

    fun onEmailClick() {
        val currentState = _accountInfoUiState.value
        if (currentState is AccountInfoUiState.Content) {
            viewModelScope.launch(handler) {
                _accountInfoDialogState.value = AccountInfoDialogState.SetEmail(
                    currentState.userInfo.email
                )
            }
        }
    }

    fun onPasswordClick() {
        val currentState = _accountInfoUiState.value
        if (currentState is AccountInfoUiState.Content) {
            viewModelScope.launch(handler) {
                _accountInfoDialogState.value = AccountInfoDialogState.SetPassword(
                    currentState.userInfo.password
                )

            }
        }
    }

    fun onDeleteAccountClick() {
        val currentState = _accountInfoUiState.value
        if (currentState is AccountInfoUiState.Content) {
            _accountInfoUiState.value = AccountInfoUiState.Loading
            viewModelScope.launch(handler) {
                when (val result = deleteAccountUseCase().toUiModel()) {
                    is DeleteResultUi.Success -> {
                        _navigationEvent.value = AccountInfoNavigationEvent.NavigateToRegistration
                    }

                    is DeleteResultUi.Error -> {
                        _accountInfoUiState.value = AccountInfoUiState.Error(result.message)
                    }
                }
            }
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}