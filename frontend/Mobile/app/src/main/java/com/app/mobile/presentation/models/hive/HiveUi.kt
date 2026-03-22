package com.app.mobile.presentation.models.hive

import com.app.mobile.presentation.models.hub.HubDetailUi
import com.app.mobile.presentation.models.queen.QueenPreviewModel

data class HiveUi(
    val name: String,
    val hub: HubDetailUi?,
    val queen: QueenPreviewModel?,
    val recentWorks: List<WorkUi> = emptyList(),
)
