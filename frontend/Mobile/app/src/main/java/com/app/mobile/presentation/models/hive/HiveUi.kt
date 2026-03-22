package com.app.mobile.presentation.models.hive

import com.app.mobile.presentation.models.hub.HubDetailUi
import com.app.mobile.presentation.models.queen.QueenUiModel

data class HiveUi(
    val name: String,
    val hub: HubDetailUi?,
    val queen: QueenUiModel?,
)
