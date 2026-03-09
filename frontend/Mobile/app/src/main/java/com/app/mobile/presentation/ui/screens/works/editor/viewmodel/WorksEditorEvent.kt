package com.app.mobile.presentation.ui.screens.works.editor.viewmodel

sealed interface WorksEditorEvent {
    data class NavigateToWorksList(val hiveId: String) : WorksEditorEvent

    data class ShowSnackBar(val message: String) : WorksEditorEvent
}