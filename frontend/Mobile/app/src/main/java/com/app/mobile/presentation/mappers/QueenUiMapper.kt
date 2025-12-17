package com.app.mobile.presentation.mappers

import com.app.mobile.domain.mappers.toHivePreview
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.domain.models.hives.queen.QueenEditorDomain
import com.app.mobile.presentation.models.hive.QueenUi
import com.app.mobile.presentation.models.queen.QueenEditorModel
import com.app.mobile.presentation.models.queen.QueenPreviewModel
import com.app.mobile.presentation.models.queen.QueenUiModel
import java.time.Instant
import java.time.ZoneId


private val UTC_ZONE = ZoneId.of("UTC")

fun QueenDomain?.toUiModel(): QueenUi {
    return this?.let { queen ->
        QueenUi.Present(
            queen = QueenPreviewModel(
                id = queen.id,
                name = queen.name,
                stage = this.stages.toCurrentStageUi()
            )
        )
    } ?: QueenUi.Absent
}

fun QueenDomain.toUiModel(hive: HiveDomainPreview?) = QueenUiModel(
    id = this.id,
    hive = hive?.toHivePreview(),
    name = this.name,
    timeline = this.stages.toTimelineUi()
)

fun QueenDomain.toPreviewModel() = QueenPreviewModel(
    id = this.id,
    name = this.name,
    stage = this.stages.toCurrentStageUi()
)

fun QueenEditorDomain.toPresentation(hives: List<HiveDomainPreview>) = QueenEditorModel(
    id = this.id,
    name = this.name,
    birthDate = this.birthDate.atStartOfDay(UTC_ZONE).toInstant().toEpochMilli(),
    hives = hives.map { it.toHivePreview() },
    hiveId = this.hiveId
)

fun QueenDomain.toEditor(hives: List<HiveDomainPreview>) = QueenEditorModel(
    id = this.id,
    hiveId = this.hiveId,
    name = this.name,
    birthDate = this.stages.birthDate.atStartOfDay(UTC_ZONE).toInstant().toEpochMilli(),
    hives = hives.map { it.toHivePreview() }
)

fun QueenEditorModel.toDomain() = QueenEditorDomain(
    id = this.id,
    name = this.name,
    hiveId = this.hiveId,
    birthDate = Instant
        .ofEpochMilli(this.birthDate)
        .atZone(UTC_ZONE)
        .toLocalDate()
)