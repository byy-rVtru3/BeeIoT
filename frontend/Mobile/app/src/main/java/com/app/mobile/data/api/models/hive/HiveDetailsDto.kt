package com.app.mobile.data.api.models.hive

import kotlinx.serialization.Serializable

@Serializable
data class HiveDetailsDto(
    val name: String,
    val hub: String? = null,
    val queen: String? = null,
    val active: Boolean? = null
)
