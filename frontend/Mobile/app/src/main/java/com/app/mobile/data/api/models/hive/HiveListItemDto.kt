package com.app.mobile.data.api.models.hive

import kotlinx.serialization.Serializable

@Serializable
data class HiveListItemDto(
    val name: String,
    val sensor: String? = null,
    val hub: String? = null,
    val queen: String? = null
)
