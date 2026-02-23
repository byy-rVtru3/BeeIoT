package com.app.mobile.presentation.ui.screens.queen.list.viewmodel

sealed interface QueenListEvent {

	data class NavigateToQueen(val queenId: String) : QueenListEvent
	data object NavigateToAddQueen : QueenListEvent

	data class ShowSnackBar(val message: String) : QueenListEvent
}
