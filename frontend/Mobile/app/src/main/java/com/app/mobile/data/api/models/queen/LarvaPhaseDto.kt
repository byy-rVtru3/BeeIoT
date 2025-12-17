package com.app.mobile.data.api.models.queen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LarvaPhaseDto(
    @SerialName("start") val start: String,
    @SerialName("day_1") val day1: String,
    @SerialName("day_2") val day2: String,
    @SerialName("day_3") val day3: String,
    @SerialName("day_4") val day4: String,
    @SerialName("day_5") val day5: String,
    @SerialName("sealed") val sealed: String
)