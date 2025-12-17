package com.app.mobile.domain.mappers

import com.app.mobile.data.database.entity.NotificationEntity
import com.app.mobile.data.database.entity.NotificationType
import com.app.mobile.domain.models.hives.NotificationDomain
import com.app.mobile.domain.models.hives.NotificationDomainType
import com.app.mobile.presentation.models.hive.NotificationTypeUi
import com.app.mobile.presentation.models.hive.NotificationUi

fun NotificationEntity.toDomain() = NotificationDomain(
    id = this.id,
    hiveId = this.hiveId,
    notificationType = this.notificationType.toDomain(),
    message = this.message,
    dateTime = this.dateTime
)

private fun NotificationType.toDomain() = NotificationDomainType.valueOf(this.name)

private fun NotificationDomainType.toUiModel() = when (this) {
    NotificationDomainType.REGULAR -> NotificationTypeUi("Обычное")
    NotificationDomainType.CRITICAL -> NotificationTypeUi("Критическое")
}

fun NotificationDomain.toUiModel() = NotificationUi(
    notificationType = this.notificationType.toUiModel(),
    message = this.message,
    dateTime = localDateTimeFormatter(this.dateTime)
)