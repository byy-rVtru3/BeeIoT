package com.app.mobile.data.api.models.hub

import kotlinx.serialization.Serializable

@Serializable
data class UpdateHubRequest(val id: String, val name: String? = null)
