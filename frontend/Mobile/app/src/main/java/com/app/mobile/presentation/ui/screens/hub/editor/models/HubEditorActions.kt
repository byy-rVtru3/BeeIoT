package com.app.mobile.presentation.ui.screens.hub.editor.models

data class HubEditorActions(
    val onNameChange: (String) -> Unit,
    val onIpAddressChange: (String) -> Unit,
    val onPortChange: (String) -> Unit,
    val onSaveClick: () -> Unit
)
