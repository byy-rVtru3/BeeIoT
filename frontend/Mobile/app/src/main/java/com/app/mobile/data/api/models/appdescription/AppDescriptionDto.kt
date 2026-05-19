package com.app.mobile.data.api.models.appdescription

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppDescriptionDto(
    val title: String = "",
    val short: String = "",
    val full: String = "",
    @SerialName("updated_by")
    val updatedBy: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
