package com.app.mobile.data.api.models.hive

import kotlinx.serialization.Serializable

@Serializable
data class HiveDetailsResponse(
    val status: String,
    val message: String,
    val data: HiveDetailsDto? = null
)
