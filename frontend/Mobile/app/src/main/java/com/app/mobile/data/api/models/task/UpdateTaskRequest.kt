package com.app.mobile.data.api.models.task

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTaskRequest(
    val id: String,
    val title: String? = null,
    val description: String? = null
)
