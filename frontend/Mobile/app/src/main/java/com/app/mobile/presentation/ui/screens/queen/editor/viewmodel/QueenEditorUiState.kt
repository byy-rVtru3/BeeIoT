package com.app.mobile.presentation.ui.screens.queen.editor.viewmodel

import com.app.mobile.presentation.models.queen.QueenEditorModel

sealed interface QueenEditorUiState {
    data object Loading : QueenEditorUiState

    data class Content(val queenEditorModel: QueenEditorModel) : QueenEditorUiState

    data class Error(val message: String) : QueenEditorUiState
}