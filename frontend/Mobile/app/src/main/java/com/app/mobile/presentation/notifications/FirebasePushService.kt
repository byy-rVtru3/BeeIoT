package com.app.mobile.presentation.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject
import kotlin.getValue

class FirebasePushService : FirebaseMessagingService() {

	private val pushController: PushController by inject()

	override fun onNewToken(token: String) {
		super.onNewToken(token)
		pushController.onNewToken()
	}

	override fun onMessageReceived(remoteMessage: RemoteMessage) {
		super.onMessageReceived(remoteMessage)
		remoteMessage.data.let { pushController.onMessageReceived(it.toEntity()) }
	}

	override fun onDestroy() {
		super.onDestroy()
		pushController.onDestroy()
	}
}