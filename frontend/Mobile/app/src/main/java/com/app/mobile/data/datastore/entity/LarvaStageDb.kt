package com.app.mobile.data.datastore.entity

import kotlinx.serialization.Serializable

@Serializable
data class LarvaStageDb(
    val hatchDate: Long,
    val feedingDays: List<Long>,
    val sealedDate: Long
)
