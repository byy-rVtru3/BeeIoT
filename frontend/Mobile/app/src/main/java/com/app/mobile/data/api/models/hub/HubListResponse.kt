package com.app.mobile.data.api.models.hub

import kotlinx.serialization.Serializable

@Serializable
data class HubListResponse(
    val status: String,
    val message: String,
    val data: List<HubListItemDto>? = null
)
