package com.app.mobile.presentation.ui.screens.hive.editor.viewmodel

sealed interface HiveEditorEvent {

    data object NavigateToCreateQueen : HiveEditorEvent

    data object NavigateToCreateHub : HiveEditorEvent

    data object NavigateBack : HiveEditorEvent

    data class ShowSnackBar(val message: String) : HiveEditorEvent
}