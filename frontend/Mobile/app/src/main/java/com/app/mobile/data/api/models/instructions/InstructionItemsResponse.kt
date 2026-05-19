package com.app.mobile.data.api.models.instructions

import kotlinx.serialization.Serializable

@Serializable
data class InstructionItemsResponse(
    val status: String,
    val message: String,
    val data: List<InstructionItemDto>? = null
)
