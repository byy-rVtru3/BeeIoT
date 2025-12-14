package com.app.mobile.data.database.entity

import kotlinx.serialization.Serializable

@Serializable
data class AdultStageDb(
    val emergenceStart: Long,
    val emergenceEnd: Long,
    val maturationStart: Long,
    val maturationEnd: Long,
    val matingFlightStart: Long,
    val matingFlightEnd: Long,
    val inseminationStart: Long,
    val inseminationEnd: Long,
    val checkLayingStart: Long,
    val checkLayingEnd: Long
)