package com.app.mobile.presentation.ui.screens.accountinfo.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toPresentation
import com.app.mobile.domain.usecase.GetAccountInfoUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class AccountInfoViewModel(
    private val getAccountInfoUseCase: GetAccountInfoUseCase
) : ViewModel() {

    private val _accountInfoUiState = MutableLiveData<AccountInfoUiState>()
    val accountInfoUiState: LiveData<AccountInfoUiState> = _accountInfoUiState

    val handler = CoroutineExceptionHandler { _, exception ->
        _accountInfoUiState.value = AccountInfoUiState.Error(exception.message.toString())
        Log.e("AccountInfoViewModel", exception.message.toString())
    }

    fun getAccountInfo() {
        val currentState = _accountInfoUiState.value
        if (currentState is AccountInfoUiState.Content) {
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

    }
}