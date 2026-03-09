package com.app.mobile.presentation.ui.screens.queen.editor.viewmodel

sealed interface QueenEditorEvent {
    data object NavigateBack : QueenEditorEvent

    data class ShowSnackBar(val message: String) : QueenEditorEvent
}