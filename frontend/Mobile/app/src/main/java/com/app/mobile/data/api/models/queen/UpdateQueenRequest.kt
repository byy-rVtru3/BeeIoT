package com.app.mobile.data.api.models.queen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateQueenRequest(
    @SerialName("old_name") val oldName: String,
    @SerialName("new_name") val newName: String? = null,
    @SerialName("start_date") val startDate: String? = null
)
