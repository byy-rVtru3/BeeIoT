package com.app.mobile.domain.models.notifications

import kotlinx.serialization.Serializable

@Serializable
data class NotificationRecord(
    val id: String,
    val title: String,
    val body: String,
    val type: String,
    val timestamp: Long
)
