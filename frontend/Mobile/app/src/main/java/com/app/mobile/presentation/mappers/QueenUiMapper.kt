package com.app.mobile.presentation.mappers

import com.app.mobile.domain.mappers.toHivePreview
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.models.hives.queen.QueenAddDomain
import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.presentation.models.hive.QueenUi
import com.app.mobile.presentation.models.queen.QueenAddModel
import com.app.mobile.presentation.models.queen.QueenUiModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


private val UTC_ZONE = ZoneId.of("UTC")

fun QueenDomain?.toUiModel(): QueenUi {
    return this?.let { queen ->
        QueenUi.Present(
            id = queen.id,
            name = queen.name,
            stage = queen.stages.toCurrentStageUi()
        )
    } ?: QueenUi.Absent
}

fun QueenDomain.toUiModel(hive: HiveDomainPreview?) = QueenUiModel(
    id = this.id,
    hive = hive?.toHivePreview(),
    name = this.name,
    timeline = this.stages.toTimelineUi()
)

fun QueenAddDomain.toPresentation(hives: List<HiveDomainPreview>?) = QueenAddModel(
    id = this.id,
    name = this.name,
    birthDate = this.birthDate.atStartOfDay(UTC_ZONE).toInstant().toEpochMilli(),
    hives = hives?.map { it.toHivePreview() } ?: emptyList(),
    hiveId = this.hiveId
)

fun QueenAddModel.toDomain() = QueenAddDomain(
    id = this.id,
    name = this.name,
    hiveId = this.hiveId,
    birthDate = LocalDate.ofInstant(Instant.ofEpochMilli(this.birthDate), UTC_ZONE)
)