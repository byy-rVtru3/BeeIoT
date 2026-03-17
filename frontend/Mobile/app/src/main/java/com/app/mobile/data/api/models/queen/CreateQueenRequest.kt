package com.app.mobile.data.api.models.queen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateQueenRequest(
    val name: String,
    @SerialName("start_date") val startDate: String
)
