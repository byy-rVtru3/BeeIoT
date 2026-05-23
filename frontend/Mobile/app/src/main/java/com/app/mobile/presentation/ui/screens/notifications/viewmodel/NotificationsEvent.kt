package com.app.mobile.presentation.ui.screens.notifications.viewmodel

sealed interface NotificationsEvent {
    data object NavigateBack : NotificationsEvent
}
