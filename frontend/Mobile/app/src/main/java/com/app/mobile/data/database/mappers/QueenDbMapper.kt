package com.app.mobile.data.database.mappers

import com.app.mobile.core.extensions.toDatabaseValue
import com.app.mobile.core.extensions.toLocalDate
import com.app.mobile.data.database.entity.AdultStageDb
import com.app.mobile.data.database.entity.EggStageDb
import com.app.mobile.data.database.entity.LarvaStageDb
import com.app.mobile.data.database.entity.PupaStageDb
import com.app.mobile.data.database.entity.QueenEntity
import com.app.mobile.data.database.entity.QueenLifecycleDbModel
import com.app.mobile.domain.models.DateRange
import com.app.mobile.domain.models.hives.queen.AdultStage
import com.app.mobile.domain.models.hives.queen.EggStage
import com.app.mobile.domain.models.hives.queen.LarvaStage
import com.app.mobile.domain.models.hives.queen.PupaStage
import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.domain.models.hives.queen.QueenLifecycle

fun QueenEntity.toDomain() = QueenDomain(
    id = this.id,
    hiveId = this.hiveId,
    name = this.name,
    stages = this.stages.toDomain()
)

fun QueenDomain.toEntity() = QueenEntity(
    id = this.id,
    hiveId = this.hiveId,
    name = this.name,
    stages = this.stages.toEntity()
)

private fun QueenLifecycleDbModel.toDomain() = QueenLifecycle(
    egg = egg.toDomain(),
    larva = larva.toDomain(),
    pupa = pupa.toDomain(),
    adult = adult.toDomain()
)

fun QueenLifecycle.toEntity() = QueenLifecycleDbModel(
    egg = egg.toEntity(),
    larva = larva.toEntity(),
    pupa = pupa.toEntity(),
    adult = adult.toEntity()
)

private fun EggStageDb.toDomain() = EggStage(
    day0Standing = day0Standing.toLocalDate(),
    day1Tilted = day1Tilted.toLocalDate(),
    day2Lying = day2Lying.toLocalDate()
)

private fun LarvaStageDb.toDomain() = LarvaStage(
    hatchDate = hatchDate.toLocalDate(),
    feedingDays = feedingDays.map { it.toLocalDate() },
    sealedDate = sealedDate.toLocalDate()
)

private fun PupaStageDb.toDomain() = PupaStage(
    period = DateRange(periodStart.toLocalDate(), periodEnd.toLocalDate()),
    selectionDate = selectionDate.toLocalDate()
)

private fun AdultStageDb.toDomain() = AdultStage(
    emergence = DateRange(emergenceStart.toLocalDate(), emergenceEnd.toLocalDate()),
    maturation = DateRange(
        maturationStart.toLocalDate(),
        maturationEnd.toLocalDate()
    ),
    matingFlight = DateRange(
        matingFlightStart.toLocalDate(),
        matingFlightEnd.toLocalDate()
    ),
    insemination = DateRange(
        inseminationStart.toLocalDate(),
        inseminationEnd.toLocalDate()
    ),
    checkLaying = DateRange(
        checkLayingStart.toLocalDate(),
        checkLayingEnd.toLocalDate()
    )
)

private fun EggStage.toEntity() = EggStageDb(
    day0Standing = day0Standing.toDatabaseValue(),
    day1Tilted = day1Tilted.toDatabaseValue(),
    day2Lying = day2Lying.toDatabaseValue()
)

private fun LarvaStage.toEntity() = LarvaStageDb(
    hatchDate = hatchDate.toDatabaseValue(),
    feedingDays = feedingDays.map { it.toDatabaseValue() },
    sealedDate = sealedDate.toDatabaseValue()
)

private fun PupaStage.toEntity() = PupaStageDb(
    periodStart = period.start.toDatabaseValue(),
    periodEnd = period.end.toDatabaseValue(),
    selectionDate = selectionDate.toDatabaseValue()
)

private fun AdultStage.toEntity() = AdultStageDb(
    emergenceStart = emergence.start.toDatabaseValue(),
    emergenceEnd = emergence.end.toDatabaseValue(),
    maturationStart = maturation.start.toDatabaseValue(),
    maturationEnd = maturation.end.toDatabaseValue(),
    matingFlightStart = matingFlight.start.toDatabaseValue(),
    matingFlightEnd = matingFlight.end.toDatabaseValue(),
    inseminationStart = insemination.start.toDatabaseValue(),
    inseminationEnd = insemination.end.toDatabaseValue(),
    checkLayingStart = checkLaying.start.toDatabaseValue(),
    checkLayingEnd = checkLaying.end.toDatabaseValue()
)