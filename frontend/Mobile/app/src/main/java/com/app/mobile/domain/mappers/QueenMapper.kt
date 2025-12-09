package com.app.mobile.domain.mappers

import com.app.mobile.data.database.entity.QueenEntity
import com.app.mobile.data.database.entity.QueenStage
import com.app.mobile.domain.models.hives.QueenDomain
import com.app.mobile.domain.models.hives.QueenStageDomain
import com.app.mobile.presentation.models.hive.QueenStageUi
import com.app.mobile.presentation.models.hive.QueenUi
import com.app.mobile.presentation.models.queen.QueenUiModel

fun QueenEntity.toDomain() = QueenDomain(
    id = this.id,
    hiveId = this.hiveId,
    name = this.name,
    stage = this.stage.toDomain()
)

private fun QueenStage.toDomain() = QueenStageDomain.valueOf(this.name)

fun QueenDomain?.toUiModel(): QueenUi {
    return this?.let { queen ->
        QueenUi.Present(
            name = queen.name,
            stage = queen.stage.toUiModel()
        )
    } ?: QueenUi.Absent
}

private fun QueenStageDomain.toUiModel() = when (this) {
    QueenStageDomain.EGG -> QueenStageUi("Яйцо", "3 дня")
    QueenStageDomain.LARVA -> QueenStageUi("Личинка", "5 дней")
    QueenStageDomain.PUPA -> QueenStageUi("Куколка", "4 дня")
    QueenStageDomain.SELECTION -> QueenStageUi("Отбор", "1 день")
    QueenStageDomain.EMERGENCE -> QueenStageUi("Выход матки", "1-2 дня")
    QueenStageDomain.MATURATION -> QueenStageUi("Дозревание", "5 дней")
    QueenStageDomain.MATING_FLIGHT -> QueenStageUi("Облет", "3 дня")
    QueenStageDomain.INSEMINATION -> QueenStageUi("Осеменение", "3 дня")
    QueenStageDomain.MASONRY_CONTROL -> QueenStageUi("Контроль кладки", "3 дня")
}

fun QueenDomain.toUiModel(hiveName: String) = QueenUiModel(
    hiveId = this.hiveId,
    hiveName = hiveName,
    queenName = this.name,
    stage = this.stage.toUiModel()
)