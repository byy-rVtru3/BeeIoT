package com.app.mobile.presentation.ui.screens.hive.editor.viewmodel

import com.app.mobile.presentation.models.hive.HiveEditorModel

sealed interface HiveEditorUiState {
    data object Loading : HiveEditorUiState
    data class Content(val hiveEditorModel: HiveEditorModel) : HiveEditorUiState
    data class Error(val message: String) : HiveEditorUiState
}