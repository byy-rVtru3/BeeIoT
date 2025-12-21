package com.app.mobile.presentation.ui.screens.accountinfo.viewmodel

import android.util.Log
import com.app.mobile.domain.mappers.toPresentation
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.account.DeleteAccountUseCase
import com.app.mobile.domain.usecase.account.GetAccountInfoUseCase
import com.app.mobile.presentation.models.account.DeleteResultUi
import com.app.mobile.presentation.ui.components.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccountInfoViewModel(
    private val getAccountInfoUseCase: GetAccountInfoUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : BaseViewModel<AccountInfoUiState, AccountInfoNavigationEvent>(AccountInfoUiState.Loading) {

    private val _accountInfoDialogState =
        MutableStateFlow<AccountInfoDialogState>(AccountInfoDialogState.Hidden)
    val accountInfoDialogState = _accountInfoDialogState.asStateFlow()

    override fun handleError(exception: Throwable) {
        updateState { AccountInfoUiState.Error(exception.message ?: "Unknown error") }
        Log.e("AccountInfoViewModel", exception.message.toString())
    }

    fun getAccountInfo() {
        updateState { AccountInfoUiState.Loading }
        launch {
            val user = getAccountInfoUseCase()?.toPresentation()
            if (user != null) {
                updateState { AccountInfoUiState.Content(user) }
            } else {
                updateState { AccountInfoUiState.Error("Пользователь не найден") }
            }
        }
    }

    fun onNameClick() {
        val state = currentState
        if (state is AccountInfoUiState.Content) {
            launch {
                _accountInfoDialogState.value = AccountInfoDialogState.SetName(
                    state.userInfo.name
                )
            }
        }
    }

    fun onEmailClick() {
        val state = currentState
        if (state is AccountInfoUiState.Content) {
            launch {
                _accountInfoDialogState.value = AccountInfoDialogState.SetEmail(
                    state.userInfo.email
                )
            }
        }
    }

    fun onPasswordClick() {
        val state = currentState
        if (state is AccountInfoUiState.Content) {
            launch {
                _accountInfoDialogState.value = AccountInfoDialogState.SetPassword(
                    state.userInfo.password
                )

            }
        }
    }

    fun onDeleteAccountClick() {
        val state = currentState
        if (state is AccountInfoUiState.Content) {
            updateState { AccountInfoUiState.Loading }
            launch {
                when (val result = deleteAccountUseCase().toUiModel()) {
                    is DeleteResultUi.Success -> {
                        sendEvent(AccountInfoNavigationEvent.NavigateToRegistration)
                    }

                    is DeleteResultUi.Error -> {
                        updateState { AccountInfoUiState.Error(result.message) }
                    }
                }
            }
        }
    }

    fun onBackClick() {
        launch {
            sendEvent(AccountInfoNavigationEvent.NavigateBack)
        }
    }
}