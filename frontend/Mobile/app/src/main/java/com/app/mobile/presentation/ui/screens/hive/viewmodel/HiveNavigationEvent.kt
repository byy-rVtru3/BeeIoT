package com.app.mobile.presentation.ui.screens.hive.viewmodel

sealed class HiveNavigationEvent {
    data object NavigateToHiveList : HiveNavigationEvent()
    data class NavigateToQueenByHive(val hiveId: Int) : HiveNavigationEvent()
    data class NavigateToWorkByHive(val hiveId: Int) : HiveNavigationEvent()
    data class NavigateToNotificationByHive(val hiveId: Int) : HiveNavigationEvent()
    data class NavigateToTemperatureByHive(val hiveId: Int) : HiveNavigationEvent()
    data class NavigateToNoiseByHive(val hiveId: Int) : HiveNavigationEvent()
    data class NavigateToWeightByHive(val hiveId: Int) : HiveNavigationEvent()
    data class NavigateToHiveEdit(val hiveId: Int) : HiveNavigationEvent()
}