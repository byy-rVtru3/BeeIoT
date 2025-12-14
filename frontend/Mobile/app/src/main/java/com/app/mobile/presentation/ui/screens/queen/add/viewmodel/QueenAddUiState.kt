package com.app.mobile.presentation.ui.screens.queen.add.viewmodel

import com.app.mobile.presentation.models.queen.QueenAddModel

sealed interface QueenAddUiState {
    data object Loading : QueenAddUiState

    data class Content(val queenAddModel: QueenAddModel) : QueenAddUiState

    data class Error(val message: String) : QueenAddUiState
}