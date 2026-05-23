package com.app.mobile.presentation.ui.screens.notifications.viewmodel

import com.app.mobile.domain.models.notifications.NotificationRecord

sealed interface NotificationsUiState {
    data object Empty : NotificationsUiState
    data class Content(val items: List<NotificationRecord>) : NotificationsUiState
}
