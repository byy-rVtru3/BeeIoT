package com.app.mobile.presentation.models.hive

sealed interface QueenUiState {
    data class Present(val name: String, val stage: QueenStageUi) : QueenUiState
    data object Absent : QueenUiState
}