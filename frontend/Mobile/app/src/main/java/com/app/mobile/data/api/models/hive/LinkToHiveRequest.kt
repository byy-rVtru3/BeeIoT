package com.app.mobile.data.api.models.hive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LinkToHiveRequest(
    @SerialName("hive_name") val hiveName: String,
    @SerialName("target_name") val targetName: String
)
