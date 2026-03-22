package com.app.mobile.presentation.ui.screens.works.detail.viewmodel

import com.app.mobile.presentation.models.hive.WorkUi

sealed interface WorkDetailUiState {
    data object Loading : WorkDetailUiState
    data class Error(val message: String) : WorkDetailUiState
    data class Content(val work: WorkUi) : WorkDetailUiState
}
