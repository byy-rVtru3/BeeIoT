package com.app.mobile.data.api.models.task

import kotlinx.serialization.Serializable

@Serializable
data class TaskListResponse(
    val status: String,
    val message: String,
    val data: List<TaskItemDto>? = null
)
