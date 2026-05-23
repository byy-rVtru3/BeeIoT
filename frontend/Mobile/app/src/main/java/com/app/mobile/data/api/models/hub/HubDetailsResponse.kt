package com.app.mobile.data.api.models.hub

import kotlinx.serialization.Serializable

@Serializable
data class HubDetailsResponse(
    val status: String,
    val message: String,
    val data: HubListItemDto? = null
)
