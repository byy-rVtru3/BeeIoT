package com.app.mobile.domain.repository.notifications

import com.app.mobile.domain.models.notifications.PushTokenCreation

interface PushTokenRepository {

	suspend fun getToken(): String

	suspend fun registerPushToken(pushTokenCreation: PushTokenCreation)
}