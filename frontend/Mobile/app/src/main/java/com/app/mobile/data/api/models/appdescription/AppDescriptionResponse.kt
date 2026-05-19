package com.app.mobile.data.api.models.appdescription

import kotlinx.serialization.Serializable

@Serializable
data class AppDescriptionResponse(
    val status: String,
    val message: String,
    val data: AppDescriptionDto? = null
)
