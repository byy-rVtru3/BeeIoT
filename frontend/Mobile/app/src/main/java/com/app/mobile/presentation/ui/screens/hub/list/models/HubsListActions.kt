package com.app.mobile.presentation.ui.screens.hub.list.models

data class HubsListActions(
    val onHubClick: (String) -> Unit,
    val onCreateHubClick: () -> Unit
)
