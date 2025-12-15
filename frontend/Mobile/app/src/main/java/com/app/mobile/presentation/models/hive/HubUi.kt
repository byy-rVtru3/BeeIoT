package com.app.mobile.presentation.models.hive

sealed interface HubUi {
    data class Present(val id: String, val name: String, val ipAddress: String, val port: Int) : HubUi
    data object Absent : HubUi
}
