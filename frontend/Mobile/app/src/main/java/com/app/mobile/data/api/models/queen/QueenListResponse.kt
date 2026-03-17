package com.app.mobile.data.api.models.queen

import kotlinx.serialization.Serializable

@Serializable
data class QueenListResponse(
    val status: String,
    val message: String,
    val data: List<QueenListItemDto>? = null
)
