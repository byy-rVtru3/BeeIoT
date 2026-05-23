package com.app.mobile.presentation.ui.screens.queen.list.viewmodel

import com.app.mobile.presentation.models.queen.QueenPreviewModel

sealed interface QueenListUiState {
    data object Loading : QueenListUiState
    data class Content(val queens: List<QueenPreviewModel>, val isRefreshing: Boolean = false) : QueenListUiState
    data class Error(val message: String) : QueenListUiState
}