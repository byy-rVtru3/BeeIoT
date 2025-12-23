package com.app.mobile.presentation.models.queen

import com.app.mobile.presentation.models.hive.QueenStageUi

data class QueenPreviewModel(
    val id: String,
    val name: String,
    val stage: QueenStageUi,
    val hiveName: String?
)
