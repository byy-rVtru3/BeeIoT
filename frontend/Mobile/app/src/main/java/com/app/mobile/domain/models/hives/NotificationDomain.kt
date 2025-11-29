package com.app.mobile.domain.models.hives

import java.time.LocalDateTime

data class NotificationDomain(
    val id: Int,
    val hiveId: Int,
    val notificationType: NotificationDomainType,
    val message: String,
    val dateTime: LocalDateTime
)
