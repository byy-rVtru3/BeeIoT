package com.app.mobile.data.api.models.task

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaskItemDto(
    val id: String,
    @SerialName("hive_name") val hiveName: String,
    val title: String,
    val description: String? = null,
    @SerialName("created_at") val createdAt: String
)
