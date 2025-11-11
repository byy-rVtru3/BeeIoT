package com.app.mobile.data.api.models

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmationRequestApiModel(
    val email: String,
    val code: String
)