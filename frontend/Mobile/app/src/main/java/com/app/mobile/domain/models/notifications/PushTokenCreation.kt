package com.app.mobile.domain.models.notifications

data class PushTokenCreation(
	val deviceId: String,
	val token: String,
)