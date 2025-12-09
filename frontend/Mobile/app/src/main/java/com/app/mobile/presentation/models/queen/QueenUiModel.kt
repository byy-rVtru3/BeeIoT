package com.app.mobile.presentation.models.queen

import com.app.mobile.presentation.models.hive.QueenStageUi

data class QueenUiModel(
    val hiveId: Int,
    val hiveName: String,
    val queenName: String,
    val stage: QueenStageUi
)
