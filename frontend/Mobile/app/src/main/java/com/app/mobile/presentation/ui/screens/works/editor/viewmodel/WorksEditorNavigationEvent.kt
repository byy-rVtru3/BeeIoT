package com.app.mobile.presentation.ui.screens.works.editor.viewmodel

sealed class WorksEditorNavigationEvent {
    data class NavigateToWorksList(val hiveId: String) : WorksEditorNavigationEvent()

}