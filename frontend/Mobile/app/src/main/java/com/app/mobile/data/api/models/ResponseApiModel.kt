package com.app.mobile.data.api.models

import kotlinx.serialization.Serializable

@Serializable
data class ResponseApiModel(
    val status: String,
    val message: String,
    val data: JwtToken? = null,
)
