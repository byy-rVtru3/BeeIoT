package com.app.mobile.data.api.models.hive

import kotlinx.serialization.Serializable

@Serializable
data class CreateHiveRequest(val name: String, val sensor: String? = null)
