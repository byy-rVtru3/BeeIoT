package com.app.mobile.domain.mappers

import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.domain.models.hives.queen.QueenEditorDomain
import com.app.mobile.domain.models.hives.queen.QueenLifecycle
import com.app.mobile.domain.models.hives.queen.QueenRequestModel


fun QueenEditorDomain.toRequest() = QueenRequestModel(
    birthDate = this.birthDate
)

fun QueenEditorDomain.toDomain(stages: QueenLifecycle) = QueenDomain(
    id = this.id,
    hiveId = this.hiveId,
    name = this.name,
    stages = stages
)