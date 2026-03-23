package com.app.mobile.data.api.models.hub

import kotlinx.serialization.Serializable

@Serializable
data class CreateHubRequest(val id: String, val name: String)
