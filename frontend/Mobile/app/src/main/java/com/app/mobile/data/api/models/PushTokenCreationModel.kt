package com.app.mobile.data.api.models

import kotlinx.serialization.Serializable

@Serializable
data class PushTokenCreationModel(
	val token: String,
	val device: String,
)
