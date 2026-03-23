package com.app.mobile.presentation.ui.screens.hub.editor.viewmodel

sealed interface HubEditorEvent {
    data object NavigateBack : HubEditorEvent
    data class ShowSnackBar(val message: String) : HubEditorEvent
}
