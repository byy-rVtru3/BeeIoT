package com.app.mobile.presentation.ui.screens.works.editor.models

data class WorksEditorActions(
    val onTitleChange: (String) -> Unit,
    val onTextChange: (String) -> Unit,
    val onSaveClick: () -> Unit,
)
