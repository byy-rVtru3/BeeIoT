package com.app.mobile.presentation.ui.screens.works.list.viewmodel

sealed class WorksListNavigationEvent {
    data class NavigateToWorkEditor(val workId: String) : WorksListNavigationEvent()

    data object NavigateBack : WorksListNavigationEvent()

    data class NavigateToWorkCreate(val hiveId: String) : WorksListNavigationEvent()
}