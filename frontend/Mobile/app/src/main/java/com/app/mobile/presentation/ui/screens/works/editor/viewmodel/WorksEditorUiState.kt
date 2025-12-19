package com.app.mobile.presentation.ui.screens.works.editor.viewmodel

import com.app.mobile.presentation.models.hive.WorkUi

sealed interface WorksEditorUiState {
    data object Loading : WorksEditorUiState
    data class Error(val message: String) : WorksEditorUiState
    data class Content(val work: WorkUi) : WorksEditorUiState
}