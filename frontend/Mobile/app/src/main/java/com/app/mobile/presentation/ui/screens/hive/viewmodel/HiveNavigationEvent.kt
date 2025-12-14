package com.app.mobile.presentation.ui.screens.hive.viewmodel

sealed class HiveNavigationEvent {
    data object NavigateToHiveList : HiveNavigationEvent()
    data class NavigateToQueenByHive(val queenId: String) : HiveNavigationEvent()
    data class NavigateToWorkByHive(val hiveId: String) : HiveNavigationEvent()
    data class NavigateToNotificationByHive(val hiveId: String) : HiveNavigationEvent()
    data class NavigateToTemperatureByHive(val hiveId: String) : HiveNavigationEvent()
    data class NavigateToNoiseByHive(val hiveId: String) : HiveNavigationEvent()
    data class NavigateToWeightByHive(val hiveId: String) : HiveNavigationEvent()
    data class NavigateToHiveEdit(val hiveId: String) : HiveNavigationEvent()
}