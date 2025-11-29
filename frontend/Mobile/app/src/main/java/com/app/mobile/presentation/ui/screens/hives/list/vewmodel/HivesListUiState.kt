package com.app.mobile.presentation.ui.screens.hives.list.vewmodel

import com.app.mobile.presentation.models.hive.HivePreview

sealed interface HivesListUiState {
    data class Content(val hives: List<HivePreview>) : HivesListUiState
    data object Empty : HivesListUiState
    data object Loading : HivesListUiState
    data class Error(val message: String) : HivesListUiState
}