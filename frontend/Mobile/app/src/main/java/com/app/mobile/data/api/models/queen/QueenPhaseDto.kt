package com.app.mobile.data.api.models.queen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QueenPhaseDto(
    @SerialName("emergence_start") val emergenceStart: String,
    @SerialName("emergence_end") val emergenceEnd: String,
    @SerialName("maturation_start") val maturationStart: String,
    @SerialName("maturation_end") val maturationEnd: String,
    @SerialName("mating_flight_start") val matingFlightStart: String,
    @SerialName("mating_flight_end") val matingFlightEnd: String,
    @SerialName("insemination_start") val inseminationStart: String,
    @SerialName("insemination_end") val inseminationEnd: String,
    @SerialName("egg_laying_check_start") val checkStart: String,
    @SerialName("egg_laying_check_end") val checkEnd: String
)
