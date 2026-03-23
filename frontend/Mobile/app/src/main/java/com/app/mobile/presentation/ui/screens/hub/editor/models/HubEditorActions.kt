package com.app.mobile.presentation.ui.screens.hub.editor.models

data class HubEditorActions(
    val onNameChange: (String) -> Unit,
    val onIdChange: (String) -> Unit,
    val onSaveClick: () -> Unit
)
