package com.app.mobile.data.api.models.hive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateHiveRequest(
    @SerialName("old_name") val oldName: String,
    @SerialName("new_name") val newName: String? = null,
    val active: Boolean? = null,
    val sensor: String? = null
)
