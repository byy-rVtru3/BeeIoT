package com.app.mobile.domain.mappers

import com.app.mobile.domain.models.hives.queen.QueenDomainPreview
import com.app.mobile.domain.models.hives.queen.QueenEditorDomain
import com.app.mobile.domain.models.hives.queen.QueenRequestModel
import com.app.mobile.presentation.models.queen.QueenPreviewModel

fun QueenEditorDomain.toRequest() = QueenRequestModel(
    birthDate = this.birthDate
)

fun QueenDomainPreview.toPreviewModel() = QueenPreviewModel(
    name = this.name,
    startDate = this.startDate
)
