package com.app.mobile.presentation.ui.screens.hive.editor.viewmodel

sealed class HiveEditorNavigationEvent {

    data object NavigateToCreateQueen : HiveEditorNavigationEvent()

    data object NavigateToCreateHub : HiveEditorNavigationEvent()

    data object NavigateBack : HiveEditorNavigationEvent()
}