package com.app.mobile.presentation.ui.screens.queen.details.viewmodel

sealed class QueenNavigationEvent {
    data class NavigateToHive(val hiveId: String) : QueenNavigationEvent()
    data class NavigateToEditQueen(val queenId: String) : QueenNavigationEvent()
}
