package com.app.mobile.domain.usecase.notifications

import com.app.mobile.domain.models.notifications.PushTokenCreation
import com.app.mobile.domain.repository.notifications.DeviceIdRepository
import com.app.mobile.domain.repository.notifications.PushTokenRepository

class SendPushTokenUseCase(
	private val repository: PushTokenRepository,
	private val deviceIdRepository: DeviceIdRepository,
) {

	suspend operator fun invoke() {
		val token = repository.getToken()
		val deviceId = deviceIdRepository.get()
		val pushTokenCreation = PushTokenCreation(
			deviceId = deviceId,
			token = token,
		)
		repository.registerPushToken(pushTokenCreation)
	}
}