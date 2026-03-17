package com.app.mobile.presentation.ui.screens.queen.details.viewmodel

sealed interface QueenEvent {
    data class NavigateToHive(val hiveName: String) : QueenEvent
    data class NavigateToEditQueen(val queenName: String) : QueenEvent

    data class ShowSnackBar(val message: String) : QueenEvent

    data object NavigateBack : QueenEvent
}
