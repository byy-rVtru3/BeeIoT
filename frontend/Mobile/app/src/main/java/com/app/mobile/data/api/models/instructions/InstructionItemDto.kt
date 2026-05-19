package com.app.mobile.data.api.models.instructions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstructionItemDto(
    val id: String? = null,
    val title: String = "",
    val body: String = "",
    val numbered: Boolean = false,
    val position: Int = 0,
    @SerialName("updated_at")
    val updatedAt: String? = null
)
