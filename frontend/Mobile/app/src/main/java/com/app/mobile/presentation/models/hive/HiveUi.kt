package com.app.mobile.presentation.models.hive

data class HiveUi(
    val id: String,
    val name: String,
    val connectedHub: HubUi,
    val notifications: List<NotificationUi> = emptyList(),
    val queen: QueenUi,
    val works: List<WorkUi> = emptyList()
)
