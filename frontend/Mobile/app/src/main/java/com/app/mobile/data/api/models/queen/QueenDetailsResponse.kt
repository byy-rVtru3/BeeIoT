package com.app.mobile.data.api.models.queen

import kotlinx.serialization.Serializable

@Serializable
data class QueenDetailsResponse(
    val status: String,
    val message: String,
    val data: QueenDetailsDto? = null
)
