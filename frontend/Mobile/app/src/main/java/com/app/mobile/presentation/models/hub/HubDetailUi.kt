package com.app.mobile.presentation.models.hub

import com.app.mobile.presentation.models.hive.NotificationUi

data class HubDetailUi(
    val id: String,
    val name: String,
    val ipAddress: String,
    val port: Int,
    val notifications: List<NotificationUi> = emptyList()
)
