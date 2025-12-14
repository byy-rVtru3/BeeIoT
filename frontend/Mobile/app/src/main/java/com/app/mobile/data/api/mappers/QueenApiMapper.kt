package com.app.mobile.data.api.mappers

import com.app.mobile.core.extensions.toLocalDate
import com.app.mobile.data.api.models.queen.CalcQueenRequest
import com.app.mobile.data.api.models.queen.EggPhaseDto
import com.app.mobile.data.api.models.queen.LarvaPhaseDto
import com.app.mobile.data.api.models.queen.PupaPhaseDto
import com.app.mobile.data.api.models.queen.QueenCalendarDto
import com.app.mobile.data.api.models.queen.QueenPhaseDto
import com.app.mobile.domain.models.DateRange
import com.app.mobile.domain.models.hives.queen.AdultStage
import com.app.mobile.domain.models.hives.queen.EggStage
import com.app.mobile.domain.models.hives.queen.LarvaStage
import com.app.mobile.domain.models.hives.queen.PupaStage
import com.app.mobile.domain.models.hives.queen.QueenLifecycle
import com.app.mobile.domain.models.hives.queen.QueenRequestModel

fun QueenRequestModel.toApiModel() = CalcQueenRequest(
    startDate = this.birthDate.toString()
)

fun QueenCalendarDto.toDomain() = QueenLifecycle(
    birthDate = this.startDate.toLocalDate(),
    egg = this.eggPhase.toDomain(),
    larva = this.larvaPhase.toDomain(),
    pupa = this.pupaPhase.toDomain(),
    adult = this.queenPhase.toDomain()
)

private fun EggPhaseDto.toDomain() = EggStage(
    day0Standing = this.standing.toLocalDate(),
    day1Tilted = this.tilted.toLocalDate(),
    day2Lying = this.lying.toLocalDate()
)

private fun LarvaPhaseDto.toDomain() = LarvaStage(
    hatchDate = this.start.toLocalDate(),
    feedingDays = listOf(day1, day2, day3, day4, day5).map { it.toLocalDate() },
    sealedDate = this.sealed.toLocalDate()
)

private fun PupaPhaseDto.toDomain() = PupaStage(
    period = DateRange(start.toLocalDate(), end.toLocalDate()),
    selectionDate = selection.toLocalDate()
)

private fun QueenPhaseDto.toDomain() = AdultStage(
    emergence = DateRange(emergenceStart.toLocalDate(), emergenceEnd.toLocalDate()),
    maturation = DateRange(maturationStart.toLocalDate(), maturationEnd.toLocalDate()),
    matingFlight = DateRange(matingFlightStart.toLocalDate(), matingFlightEnd.toLocalDate()),
    insemination = DateRange(inseminationStart.toLocalDate(), inseminationEnd.toLocalDate()),
    checkLaying = DateRange(checkStart.toLocalDate(), checkEnd.toLocalDate())
)