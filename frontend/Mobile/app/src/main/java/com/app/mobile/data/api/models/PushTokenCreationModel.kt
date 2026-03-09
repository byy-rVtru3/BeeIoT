package com.app.mobile.data.api.models

import kotlinx.serialization.Serializable

@Serializable
data class PushTokenCreationModel(
	val deviceId: String,
	val token: String
)
