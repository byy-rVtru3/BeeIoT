package com.app.mobile.presentation.notifications

import com.google.firebase.messaging.RemoteMessage

internal const val TITLE = "title"
internal const val BODY = "body"
internal const val MOBILE_NOTIFICATION_TYPE = "mobile_notification_type"

fun RemoteMessage.toEntity(): NotificationDataEntity = NotificationDataEntity(
	title = data[TITLE].takeIf { !it.isNullOrBlank() } ?: notification?.title.orEmpty(),
	body = data[BODY].takeIf { !it.isNullOrBlank() } ?: notification?.body.orEmpty(),
	notificationType = when (data[MOBILE_NOTIFICATION_TYPE]) {
		"CRITICAL" -> NotificationType.CRITICAL
		"REGULAR"  -> NotificationType.REGULAR
		else       -> NotificationType.CRITICAL
	}
)