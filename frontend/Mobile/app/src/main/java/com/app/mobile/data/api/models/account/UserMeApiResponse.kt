package com.app.mobile.data.api.models.account

import kotlinx.serialization.Serializable

@Serializable
data class UserMeApiResponse(
    val status: String,
    val message: String,
    val data: UserInfoApiModel
)
