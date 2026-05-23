package com.app.mobile.presentation.models.hive

import com.app.mobile.presentation.models.queen.QueenPreviewListModel

data class HiveEditorModel(
    val name: String,
    val connectedHubId: String?,
    val hubs: List<HubPreviewModel>,
    val connectedQueenName: String?,
    val queens: List<QueenPreviewListModel>,
)
