package com.app.mobile.presentation.ui.screens.hive.details.viewmodel

sealed interface HiveEvent {
    data object NavigateToHiveList : HiveEvent
    data class NavigateToQueenByHive(val queenId: String) : HiveEvent
    data class NavigateToWorkByHive(val hiveId: String) : HiveEvent
    data class NavigateToNotificationByHive(val hiveId: String) : HiveEvent
    data class NavigateToTemperatureByHive(val hiveId: String) : HiveEvent
    data class NavigateToNoiseByHive(val hiveId: String) : HiveEvent
    data class NavigateToWeightByHive(val hiveId: String) : HiveEvent
    data class NavigateToHiveEdit(val hiveId: String) : HiveEvent

    data class ShowSnackBar(val message: String) : HiveEvent
}