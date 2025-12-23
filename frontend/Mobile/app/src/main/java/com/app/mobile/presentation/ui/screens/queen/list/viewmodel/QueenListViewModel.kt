package com.app.mobile.presentation.ui.screens.queen.list.viewmodel

import android.util.Log
import com.app.mobile.domain.usecase.hives.queen.GetQueensUseCase
import com.app.mobile.presentation.mappers.toPreviewModel
import com.app.mobile.presentation.ui.components.BaseViewModel

class QueenListViewModel(
    private val getQueensUseCase: GetQueensUseCase
) : BaseViewModel<QueenListUiState, QueenListNavigationEvent>(QueenListUiState.Loading) {

    override fun handleError(exception: Throwable) {
        updateState { QueenListUiState.Error(exception.message ?: "Unknown error") }
        Log.e("QueenListViewModel", exception.message ?: "Unknown error")
    }

    fun loadQueens() {
        updateState { QueenListUiState.Loading }
        launch {
            val queens = getQueensUseCase().map { it.toPreviewModel() }

            updateState { QueenListUiState.Content(queens) }
        }
    }

    fun onQueenClick(queenId: String) {
        if (currentState is QueenListUiState.Content) {
            sendEvent(QueenListNavigationEvent.NavigateToQueen(queenId))
        }
    }

    fun onAddClick() {
        if (currentState is QueenListUiState.Content) {
            sendEvent(QueenListNavigationEvent.NavigateToAddQueen)
        }
    }
}