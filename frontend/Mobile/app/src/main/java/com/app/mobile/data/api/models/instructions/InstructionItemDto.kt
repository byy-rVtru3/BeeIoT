package com.app.mobile.data.api.models.instructions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstructionItemDto(
    val id: Int? = null,
    val title: String = "",
    val content: String = "",
    @SerialName("created_at")
    val createdAt: Long? = null
)
