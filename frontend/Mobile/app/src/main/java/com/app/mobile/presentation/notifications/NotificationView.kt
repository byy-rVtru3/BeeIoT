package com.app.mobile.presentation.notifications

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.app.mobile.R
import com.app.mobile.presentation.navigation.Screen
import com.app.mobile.presentation.navigation.putScreenInIntent
import java.util.UUID

private const val ACTION_NOTIFICATION_CLICK = "com.app.mobile.ACTION_NOTIFICATION_CLICK"
private const val REGULAR_REQUEST_CODE = 100
private const val CRITICAL_REQUEST_CODE = 300

interface NotificationView {

	fun showRegularNotification(title: String, body: String)
	fun showCriticalNotification(title: String, body: String)
}

class NotificationViewImpl(private val context: Context) : NotificationView {

	override fun showRegularNotification(title: String, body: String) {
		showNotification(
			title = title,
			body = body,
			channelId = NotificationChannels.REGULAR_CHANNEL_ID,
			groupKey = NotificationChannels.REGULAR_CHANNEL_ID,
			category = NotificationCompat.CATEGORY_RECOMMENDATION,
			intent = Intent(ACTION_NOTIFICATION_CLICK).putScreenInIntent(Screen.SCREEN_MAIN),
			intentRequestCode = REGULAR_REQUEST_CODE
		)
	}

	override fun showCriticalNotification(title: String, body: String) {
		showNotification(
			title = title,
			body = body,
			channelId = NotificationChannels.CRITICAL_CHANNEL_ID,
			groupKey = NotificationChannels.CRITICAL_CHANNEL_ID,
			category = NotificationCompat.CATEGORY_RECOMMENDATION,
			intent = Intent(ACTION_NOTIFICATION_CLICK).putScreenInIntent(Screen.SCREEN_HIVES),
			intentRequestCode = CRITICAL_REQUEST_CODE
		)
	}

	private fun showNotification(
		title: String,
		body: String,
		groupKey: String,
		channelId: String,
		category: String,
		intent: Intent,
		intentRequestCode: Int
	) {
		createNotification(
			channelId = channelId,
			id = UUID.randomUUID().hashCode(),
			title = title,
			body = body,
			groupKey = groupKey,
			intent = intent,
			intentRequestCode = intentRequestCode,
			context = context,
			category = category,
		)

		updateSummaryNotification(
			channelId = channelId,
			context = context,
			groupKey = groupKey,
			category = category,
			summaryNotificationText = context.getString(R.string.summary_notification_text)
		)
	}
}