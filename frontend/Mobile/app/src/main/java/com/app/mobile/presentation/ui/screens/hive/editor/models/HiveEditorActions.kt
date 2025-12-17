package com.app.mobile.presentation.ui.screens.hive.editor.models

data class HiveEditorActions(
    val onNameChange: (String) -> Unit,
    val onCreateQueenClick: () -> Unit,
    val onCreateHubClick: () -> Unit,
    val onAddQueenClick: (String) -> Unit,
    val onAddHubClick: (String) -> Unit,
    val onSaveClick: () -> Unit
)
