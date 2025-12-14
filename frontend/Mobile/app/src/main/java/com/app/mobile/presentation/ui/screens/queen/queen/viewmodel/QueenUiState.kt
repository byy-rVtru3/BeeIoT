package com.app.mobile.presentation.ui.screens.queen.queen.viewmodel

import com.app.mobile.presentation.models.queen.QueenUiModel

sealed interface QueenUiState {
    object Loading : QueenUiState
    data class Error(val message: String) : QueenUiState
    data class Content(val queen: QueenUiModel) : QueenUiState
}