package com.app.mobile.presentation.ui.screens.works.list.viewmodel

sealed interface WorksListEvent {
    data class NavigateToWorkEditor(val workId: String, val hiveId: String) :
        WorksListEvent

    data object NavigateBack : WorksListEvent

    data class NavigateToWorkCreate(val hiveId: String) : WorksListEvent

    data class ShowSnackBar(val message: String) : WorksListEvent
}