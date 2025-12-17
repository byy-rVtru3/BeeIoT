package com.app.mobile.data.api.models.queen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QueenCalendarDto(
    @SerialName("start_date") val startDate: String,
    @SerialName("egg_phase") val eggPhase: EggPhaseDto,
    @SerialName("larva_phase") val larvaPhase: LarvaPhaseDto,
    @SerialName("pupa_phase") val pupaPhase: PupaPhaseDto,
    @SerialName("queen_phase") val queenPhase: QueenPhaseDto
)
