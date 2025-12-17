package com.app.mobile.presentation.ui.screens.queen.editor.viewmodel


sealed class QueenEditorNavigationEvent {
    data object NavigateBack : QueenEditorNavigationEvent()
}