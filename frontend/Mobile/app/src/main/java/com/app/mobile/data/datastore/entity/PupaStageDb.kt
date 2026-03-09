package com.app.mobile.data.datastore.entity

import kotlinx.serialization.Serializable

@Serializable
data class PupaStageDb(
    val periodStart: Long,
    val periodEnd: Long,
    val selectionDate: Long
)
