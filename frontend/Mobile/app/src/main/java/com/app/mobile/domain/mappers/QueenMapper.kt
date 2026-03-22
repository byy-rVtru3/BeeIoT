package com.app.mobile.domain.mappers

import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.domain.models.hives.queen.QueenDomainPreview
import com.app.mobile.domain.models.hives.queen.QueenEditorDomain
import com.app.mobile.domain.models.hives.queen.QueenRequestModel
import com.app.mobile.presentation.mappers.toCurrentStageUi
import com.app.mobile.presentation.models.queen.QueenPreviewListModel
import com.app.mobile.presentation.models.queen.QueenPreviewModel

fun QueenEditorDomain.toRequest() = QueenRequestModel(
	birthDate = this.birthDate
)

fun QueenDomain.toPreviewModel() = QueenPreviewModel(
	name = this.name,
	stage = this.stages.toCurrentStageUi()
)

fun QueenDomainPreview.toPreviewListModel() = QueenPreviewListModel(
	name = this.name,
	startDate = this.startDate.toString()
)