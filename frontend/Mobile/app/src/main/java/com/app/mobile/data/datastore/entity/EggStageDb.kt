package com.app.mobile.data.datastore.entity

import kotlinx.serialization.Serializable

@Serializable
data class EggStageDb(
    val day0Standing: Long,
    val day1Tilted: Long,
    val day2Lying: Long
)
