package com.app.mobile.presentation.ui.screens.hub.details.viewmodel

sealed interface HubEvent {
    data object NavigateToHubList : HubEvent
    data class NavigateToHubEdit(val hubId: String) : HubEvent
    data class NavigateToNotificationByHub(val hubId: String) : HubEvent
    data class ShowSnackBar(val message: String) : HubEvent
}
