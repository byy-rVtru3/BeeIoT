package com.app.mobile.domain.models.hives

import java.time.LocalDateTime
import java.util.UUID

data class NotificationDomain(
    val id: String = UUID.randomUUID().toString(),
    val hiveId: String,
    val notificationType: NotificationDomainType,
    val message: String,
    val dateTime: LocalDateTime
)
