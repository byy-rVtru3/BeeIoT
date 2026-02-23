package com.app.mobile.presentation.ui.screens.queen.details.viewmodel

sealed interface QueenEvent {
    data class NavigateToHive(val hiveId: String) : QueenEvent
    data class NavigateToEditQueen(val queenId: String) : QueenEvent

    data class ShowSnackBar(val message: String) : QueenEvent

    data object NavigateBack : QueenEvent
}
