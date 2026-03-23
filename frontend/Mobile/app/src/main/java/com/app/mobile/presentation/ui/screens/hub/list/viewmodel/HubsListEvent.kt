package com.app.mobile.presentation.ui.screens.hub.list.viewmodel

sealed interface HubsListEvent {
    data class NavigateToHub(val hubId: String) : HubsListEvent
    data object NavigateToCreateHub : HubsListEvent
    data class ShowSnackBar(val message: String) : HubsListEvent
}
