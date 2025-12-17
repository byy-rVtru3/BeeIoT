package com.app.mobile.data.api.models.queen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CalcQueenRequest(
    @SerialName("start_date") val startDate: String
)
