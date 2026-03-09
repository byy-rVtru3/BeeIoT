package com.app.mobile.presentation.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.app.mobile.R

interface NotificationChannelsInitializer {

	fun createChannels()
}

class NotificationChannelsInitializerImpl(
	val context: Context
) : NotificationChannelsInitializer {

	override fun createChannels() {
		createNotificationChannel(
			channelId = NotificationChannels.REGULAR_CHANNEL_ID,
			channelName = context.getString(R.string.regular_channel)
		)
		createNotificationChannel(
			channelId = NotificationChannels.CRITICAL_CHANNEL_ID,
			channelName = context.getString(R.string.critical_channel)
		)
	}

	private fun createNotificationChannel(
		channelId: String,
		channelName: String,
	) {
		val notificationManager = context.getSystemService(NotificationManager::class.java)
		val notificationChannel = NotificationChannel(
			channelId,
			channelName,
			NotificationManager.IMPORTANCE_DEFAULT
		)
		notificationManager.createNotificationChannel(notificationChannel)
	}
}