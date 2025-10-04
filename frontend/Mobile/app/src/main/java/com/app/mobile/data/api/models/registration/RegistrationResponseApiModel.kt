package com.app.mobile.data.api.models.registration

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationResponseApiModel(
    val code: Int,
    val message: String,
)
