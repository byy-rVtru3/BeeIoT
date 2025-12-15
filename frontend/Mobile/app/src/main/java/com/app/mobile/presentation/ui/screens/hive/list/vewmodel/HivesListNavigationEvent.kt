package com.app.mobile.presentation.ui.screens.hive.list.vewmodel

sealed class HivesListNavigationEvent {
    data class NavigateToHive(val hiveId: String) : HivesListNavigationEvent()
    data object NavigateToCreateHive : HivesListNavigationEvent()
}