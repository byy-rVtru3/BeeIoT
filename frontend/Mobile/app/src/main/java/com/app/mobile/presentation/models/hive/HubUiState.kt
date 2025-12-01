package com.app.mobile.presentation.models.hive

sealed interface HubUiState {
    data class Present(val name: String, val ipAddress: String, val port: Int) : HubUiState
    data object Absent : HubUiState
}
