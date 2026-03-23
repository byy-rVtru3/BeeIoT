package com.app.mobile.presentation.ui.screens.works.list.viewmodel

import com.app.mobile.presentation.models.hive.WorkUi

sealed interface WorksListUiState {
    data object Loading : WorksListUiState
    data class Content(val works: List<WorkUi>, val isRefreshing: Boolean = false) : WorksListUiState
    data class Error(val message: String) : WorksListUiState
}