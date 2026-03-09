package com.app.mobile.domain.scenario

import com.app.mobile.domain.repository.notifications.TokenRetryScheduler
import com.app.mobile.domain.usecase.notifications.SendPushTokenUseCase
import kotlinx.coroutines.CancellationException

class RegisterPushTokenScenario(
	private val sendPushTokenUseCase: SendPushTokenUseCase,
	private val tokenRetryScheduler: TokenRetryScheduler,
) {

	suspend operator fun invoke() {
		try {
			sendPushTokenUseCase()
		} catch (ce: CancellationException) {
			tokenRetryScheduler.scheduleRetry()
			throw ce
		} catch (_: Exception) {
			tokenRetryScheduler.scheduleRetry()
		}
	}
}