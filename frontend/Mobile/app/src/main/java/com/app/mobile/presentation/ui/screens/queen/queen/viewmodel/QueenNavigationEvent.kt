package com.app.mobile.presentation.ui.screens.queen.queen.viewmodel

sealed class QueenNavigationEvent {
    data object NavigateToHive : QueenNavigationEvent()
    data class NavigateToEditQueen(val queenId: String) : QueenNavigationEvent()
}
