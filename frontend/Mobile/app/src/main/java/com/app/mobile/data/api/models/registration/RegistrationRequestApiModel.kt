package com.app.mobile.data.api.models.registration

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationRequestApiModel(
    val email: String,
    val password: String,
)
