package com.app.mobile.presentation.ui.screens.accountinfo.viewmodel

import com.app.mobile.presentation.models.account.UserInfoModel

sealed interface AccountInfoUiState {
    data class Content(val userInfo: UserInfoModel) : AccountInfoUiState
    data object Loading : AccountInfoUiState
    data class Error(val message: String) : AccountInfoUiState
}