package com.app.mobile.presentation.models.hive

data class HiveUi(
    val id: Int,
    val name: String,
    val connectedHub: HubUiState,
    val notifications: List<NotificationUi> = emptyList(),
    val queen: QueenUiState,
    val works: List<WorkUi> = emptyList()
)
