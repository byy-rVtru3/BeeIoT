package com.app.mobile.presentation.notifications

internal const val TITLE = "title"
internal const val BODY = "body"
internal const val MOBILE_NOTIFICATION_TYPE = "mobile_notification_type"

fun Map<String, String>.toEntity(): NotificationDataEntity =
	NotificationDataEntity(
		title = this[TITLE].orEmpty(),
		body = this[BODY].orEmpty(),
		notificationType = when (this[MOBILE_NOTIFICATION_TYPE]) {
			"CRITICAL" -> NotificationType.CRITICAL
			"REGULAR"  -> NotificationType.REGULAR
			else       -> NotificationType.CRITICAL
		}
	)