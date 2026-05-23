package com.app.mobile.presentation.mappers

import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.domain.models.hives.queen.QueenEditorDomain
import com.app.mobile.presentation.models.queen.QueenEditorModel
import com.app.mobile.presentation.models.queen.QueenUiModel
import java.time.ZoneId

private val UTC_ZONE = ZoneId.of("UTC")

fun QueenDomain.toUiModel() = QueenUiModel(
    name = this.name,
    timeline = this.stages.toTimelineUi()
)

fun QueenEditorDomain.toPresentation() = QueenEditorModel(
    name = this.name,
    birthDate = this.birthDate.atStartOfDay(UTC_ZONE).toInstant().toEpochMilli(),
    isNew = true
)

fun QueenDomain.toEditor() = QueenEditorModel(
    name = this.name,
    birthDate = this.stages.birthDate.atStartOfDay(UTC_ZONE).toInstant().toEpochMilli(),
    isNew = false
)
