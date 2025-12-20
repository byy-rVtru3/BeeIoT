package com.app.mobile.presentation.ui.screens.accountinfo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toPresentation
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.account.DeleteAccountUseCase
import com.app.mobile.domain.usecase.account.GetAccountInfoUseCase
import com.app.mobile.presentation.models.account.DeleteResultUi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AccountInfoViewModel(
    private val getAccountInfoUseCase: GetAccountInfoUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _accountInfoUiState =
        MutableStateFlow<AccountInfoUiState>(AccountInfoUiState.Loading)
    val accountInfoUiState = _accountInfoUiState.asStateFlow()

    private val _accountInfoDialogState =
        MutableStateFlow<AccountInfoDialogState>(AccountInfoDialogState.Hidden)
    val accountInfoDialogState = _accountInfoDialogState.asStateFlow()

    private val _navigationEvent = Channel<AccountInfoNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

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
                        _navigationEvent.send(AccountInfoNavigationEvent.NavigateToRegistration)
                    }

                    is DeleteResultUi.Error -> {
                        _accountInfoUiState.value = AccountInfoUiState.Error(result.message)
                    }
                }
            }
        }
    }

    fun onBackClick() {
        viewModelScope.launch(handler) {
            _navigationEvent.send(AccountInfoNavigationEvent.NavigateBack)
        }
    }
}