package com.app.mobile.presentation.ui.screens.hive.details.viewmodel

sealed interface HiveEvent {
    data object NavigateToHiveList : HiveEvent
    data class NavigateToQueenByHive(val queenName: String) : HiveEvent
    data class NavigateToWorkByHive(val hiveName: String) : HiveEvent
    data class NavigateToNotificationByHive(val hiveName: String) : HiveEvent
    data class NavigateToTemperatureByHive(val hiveName: String) : HiveEvent
    data class NavigateToNoiseByHive(val hiveName: String) : HiveEvent
    data class NavigateToWeightByHive(val hiveName: String) : HiveEvent
    data class NavigateToHiveEdit(val hiveName: String) : HiveEvent
    data class NavigateToWorkDetail(val workId: String, val hiveName: String) : HiveEvent

    data class ShowSnackBar(val message: String) : HiveEvent
}
