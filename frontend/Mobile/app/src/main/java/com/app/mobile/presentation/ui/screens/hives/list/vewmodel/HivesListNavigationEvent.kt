package com.app.mobile.presentation.ui.screens.hives.list.vewmodel

sealed class HivesListNavigationEvent {
    data class NavigateToHive(val hiveId: String) : HivesListNavigationEvent()
    data object NavigateToCreateHive : HivesListNavigationEvent()
}