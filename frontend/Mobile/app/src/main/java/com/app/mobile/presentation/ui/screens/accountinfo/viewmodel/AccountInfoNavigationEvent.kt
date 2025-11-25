package com.app.mobile.presentation.ui.screens.accountinfo.viewmodel

sealed class AccountInfoNavigationEvent {
    data object NavigateToRegistration : AccountInfoNavigationEvent()
}