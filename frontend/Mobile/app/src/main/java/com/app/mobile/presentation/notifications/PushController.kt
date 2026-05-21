package com.app.mobile.presentation.notifications

import com.app.mobile.domain.models.notifications.NotificationRecord
import com.app.mobile.domain.scenario.RegisterPushTokenScenario
import com.app.mobile.domain.usecase.notifications.SaveNotificationUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.UUID

class PushController(
	private val registerPushTokenScenario: RegisterPushTokenScenario,
	private val notificationView: NotificationView,
	private val saveNotificationUseCase: SaveNotificationUseCase,
	private val dispatcher: CoroutineDispatcher
) {

	private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)

	fun onNewToken() {
		scope.launch { registerPushTokenScenario() }
	}

	fun onMessageReceived(push: NotificationDataEntity) {
		scope.launch {
			saveNotificationUseCase(
				NotificationRecord(
					id = UUID.randomUUID().toString(),
					title = push.title,
					body = push.body,
					type = push.notificationType.name,
					timestamp = System.currentTimeMillis()
				)
			)
		}
		when (push.notificationType) {
			NotificationType.REGULAR  -> notificationView.showRegularNotification(
				title = push.title,
				body = push.body,
			)
			NotificationType.CRITICAL -> notificationView.showCriticalNotification(
				title = push.title,
				body = push.body,
			)
		}
	}

	fun onDestroy() {
		scope.cancel()
	}
}