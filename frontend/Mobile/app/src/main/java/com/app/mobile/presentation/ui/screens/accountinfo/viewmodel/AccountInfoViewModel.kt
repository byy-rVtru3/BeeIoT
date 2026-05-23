package com.app.mobile.presentation.ui.screens.accountinfo.viewmodel

import android.util.Log
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.mappers.toDeleteUiModel
import com.app.mobile.domain.mappers.toPresentation
import com.app.mobile.domain.usecase.account.DeleteAccountUseCase
import com.app.mobile.domain.usecase.account.GetAccountInfoUseCase
import com.app.mobile.domain.usecase.account.UpdateEmailUseCase
import com.app.mobile.domain.usecase.account.UpdateNameUseCase
import com.app.mobile.domain.usecase.account.UpdatePasswordUseCase
import com.app.mobile.presentation.models.account.DeleteResultUi
import com.app.mobile.presentation.models.account.TypeConfirmationUi
import com.app.mobile.presentation.ui.components.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccountInfoViewModel(
    private val getAccountInfoUseCase: GetAccountInfoUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val updateNameUseCase: UpdateNameUseCase,
    private val updateEmailUseCase: UpdateEmailUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase
) : BaseViewModel<AccountInfoUiState, AccountInfoEvent>(AccountInfoUiState.Loading) {

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
            when (val result = getAccountInfoUseCase()) {
                is ApiResult.Success -> updateState {
                    AccountInfoUiState.Content(result.data.toPresentation())
                }

                else -> {
                    updateState { AccountInfoUiState.Error("Не удалось загрузить данные аккаунта") }
                    sendEvent(AccountInfoEvent.NavigateToRegistration)
                }
            }
        }
    }

    fun refresh() {
        val current = currentState as? AccountInfoUiState.Content ?: return
        updateState { current.copy(isRefreshing = true) }
        launch {
            when (val result = getAccountInfoUseCase()) {
                is ApiResult.Success -> updateState {
                    AccountInfoUiState.Content(result.data.toPresentation())
                }

                else -> {
                    updateState { current.copy(isRefreshing = false) }
                    sendEvent(AccountInfoEvent.ShowSnackBar("Ошибка загрузки данных"))
                }
            }
        }
    }

    fun resetError() = getAccountInfo()

    fun onNameClick() {
        val state = currentState
        if (state is AccountInfoUiState.Content) {
            _accountInfoDialogState.value = AccountInfoDialogState.SetName(state.userInfo.name)
        }
    }

    fun onEmailClick() {
        val state = currentState
        if (state is AccountInfoUiState.Content) {
            _accountInfoDialogState.value = AccountInfoDialogState.SetEmail(state.userInfo.email)
        }
    }

    fun onPasswordClick() {
        _accountInfoDialogState.value = AccountInfoDialogState.SetPassword
    }

    fun dismissDialog() {
        _accountInfoDialogState.value = AccountInfoDialogState.Hidden
    }

    fun submitName(name: String) {
        dismissDialog()
        launch {
            when (val result = updateNameUseCase(name)) {
                is ApiResult.Success -> {
                    sendEvent(AccountInfoEvent.ShowSnackBar("Имя успешно изменено"))
                    getAccountInfo()
                }

                else -> sendEvent(AccountInfoEvent.ShowSnackBar(result.toErrorMessage()))
            }
        }
    }

    fun submitEmail(email: String) {
        dismissDialog()
        launch {
            when (val result = updateEmailUseCase(email)) {
                is ApiResult.Success -> {
                    sendEvent(AccountInfoEvent.ShowSnackBar("Email успешно изменён"))
                    getAccountInfo()
                }

                else -> sendEvent(AccountInfoEvent.ShowSnackBar(result.toErrorMessage()))
            }
        }
    }

    fun submitPassword(newPassword: String) {
        val state = currentState as? AccountInfoUiState.Content ?: return
        dismissDialog()
        launch {
            when (val result = updatePasswordUseCase(state.userInfo.email, newPassword)) {
                is ApiResult.Success -> sendEvent(
                    AccountInfoEvent.NavigateToConfirmation(
                        email = state.userInfo.email,
                        type = TypeConfirmationUi.CHANGE_PASSWORD
                    )
                )

                else -> sendEvent(AccountInfoEvent.ShowSnackBar(result.toErrorMessage()))
            }
        }
    }

    fun onDeleteAccountClick() {
        val state = currentState
        if (state is AccountInfoUiState.Content) {
            updateState { AccountInfoUiState.Loading }
            launch {
                when (val result = deleteAccountUseCase().toDeleteUiModel()) {
                    is DeleteResultUi.Success -> {
                        sendEvent(AccountInfoEvent.NavigateToRegistration)
                    }

                    is DeleteResultUi.Error -> {
                        sendEvent(AccountInfoEvent.ShowSnackBar(result.message))
                    }
                }
            }
        }
    }

    fun onBackClick() {
        launch {
            sendEvent(AccountInfoEvent.NavigateBack)
        }
    }
}
