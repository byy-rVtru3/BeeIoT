package com.app.mobile.presentation.ui.screens.queen.list.viewmodel

sealed class QueenListNavigationEvent {

    data class NavigateToQueen(val queenId: String) : QueenListNavigationEvent()
    data object NavigateToAddQueen : QueenListNavigationEvent()

}
