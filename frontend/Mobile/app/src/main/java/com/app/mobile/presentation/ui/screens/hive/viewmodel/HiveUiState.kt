package com.app.mobile.presentation.ui.screens.hive.viewmodel

import com.app.mobile.presentation.models.hive.HiveUi

sealed interface HiveUiState {
    data object Loading : HiveUiState
    data class Error(val message: String) : HiveUiState
    data class Content(val hive: HiveUi) : HiveUiState
}