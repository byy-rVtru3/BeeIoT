package com.app.mobile.domain.mappers

import com.app.mobile.data.database.entity.QueenEntity
import com.app.mobile.data.database.entity.QueenStage
import com.app.mobile.domain.models.hives.Queen
import com.app.mobile.domain.models.hives.QueenStageDomain

fun QueenEntity.toDomain() = Queen(
    id = this.id,
    hiveId = this.hiveId,
    stage = this.stage.toDomain()
)

private fun QueenStage.toDomain() = QueenStageDomain.valueOf(this.name)