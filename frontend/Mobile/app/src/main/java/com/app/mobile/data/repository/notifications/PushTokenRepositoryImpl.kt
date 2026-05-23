package com.app.mobile.data.repository.notifications

import com.app.mobile.data.datastore.FcmTokenDataSource
import com.app.mobile.domain.models.notifications.PushTokenCreation
import com.app.mobile.domain.repository.RepositoryApi
import com.app.mobile.domain.repository.notifications.PushTokenRepository

class PushTokenRepositoryImpl(
	private val fcmTokenDataSource: FcmTokenDataSource,
	private val repositoryApi: RepositoryApi,
) : PushTokenRepository {

	override suspend fun getToken(): String = fcmTokenDataSource.getToken()

	override suspend fun registerPushToken(pushTokenCreation: PushTokenCreation) {
		repositoryApi.registerPushToken(pushTokenCreation)
	}
}