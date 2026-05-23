package com.app.mobile.data.api.models.task

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequest(
    @SerialName("hive_name") val hiveName: String,
    val title: String,
    val description: String? = null
)
