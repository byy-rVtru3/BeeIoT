package com.app.mobile.presentation.ui.screens.hive.list.vewmodel

sealed interface HivesListEvent {
    data class NavigateToHive(val hiveId: String) : HivesListEvent
    data object NavigateToCreateHive : HivesListEvent

    data class ShowSnackBar(val message: String) : HivesListEvent
}