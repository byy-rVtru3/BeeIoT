package com.app.mobile.data.api.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequestApiModel(
    val email: String,
    val password: String,
)
