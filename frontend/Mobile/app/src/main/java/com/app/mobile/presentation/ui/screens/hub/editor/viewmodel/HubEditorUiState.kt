package com.app.mobile.presentation.ui.screens.hub.editor.viewmodel

import com.app.mobile.presentation.models.hub.HubEditorModel

sealed interface HubEditorUiState {
    data object Loading : HubEditorUiState
    data class Error(val message: String) : HubEditorUiState
    data class Content(val hubEditorModel: HubEditorModel) : HubEditorUiState
}
