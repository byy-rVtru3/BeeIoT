package com.app.mobile.data.api.models.hive

import kotlinx.serialization.Serializable

@Serializable
data class HiveListResponse(
    val status: String,
    val message: String,
    val data: List<HiveListItemDto>? = null
)
