package com.app.mobile.presentation.notifications

import com.app.mobile.domain.scenario.RegisterPushTokenScenario
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PushController(
	private val registerPushTokenScenario: RegisterPushTokenScenario,
	private val notificationView: NotificationView,
	private val dispatcher: CoroutineDispatcher
) {

	private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)

	fun onNewToken() {
		scope.launch { registerPushTokenScenario() }
	}

	fun onMessageReceived(push: NotificationDataEntity) {
		when (push.notificationType) {
			NotificationType.REGULAR   -> notificationView.showRegularNotification(
				title = push.title,
				body = push.body,
			)

			NotificationType.CRITICAL  -> notificationView.showCriticalNotification(
				title = push.title,
				body = push.body,
			)

		}
	}

	fun onDestroy() {
		scope.cancel()
	}
}