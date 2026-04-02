package com.app.mobile.data.api.models.account

import kotlinx.serialization.Serializable

@Serializable
data class UserInfoApiModel(
    val email: String,
    val name: String
)
