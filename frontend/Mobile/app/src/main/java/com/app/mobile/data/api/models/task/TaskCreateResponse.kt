package com.app.mobile.data.api.models.task

import kotlinx.serialization.Serializable

@Serializable
data class TaskCreateResponse(
    val status: String,
    val message: String,
    val data: TaskItemDto? = null
)
