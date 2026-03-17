package com.app.mobile.presentation.ui.screens.queen.list.viewmodel

import android.util.Log
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.mappers.toPreviewModel
import com.app.mobile.domain.usecase.hives.queen.GetQueensUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel

class QueenListViewModel(
    private val getQueensUseCase: GetQueensUseCase
) : BaseViewModel<QueenListUiState, QueenListEvent>(QueenListUiState.Loading) {

    override fun handleError(exception: Throwable) {
        updateState { QueenListUiState.Error(exception.message ?: "Unknown error") }
        Log.e("QueenListViewModel", exception.message ?: "Unknown error")
    }

    fun loadQueens() {
        updateState { QueenListUiState.Loading }
        launch {
            when (val result = getQueensUseCase()) {
                is ApiResult.Success -> {
                    val queens = result.data.map { it.toPreviewModel() }
                    updateState { QueenListUiState.Content(queens) }
                }

                else -> {
                    updateState { QueenListUiState.Error(result.toErrorMessage()) }
                }
            }
        }
    }

    fun onQueenClick(queenName: String) {
        if (currentState is QueenListUiState.Content) {
            sendEvent(QueenListEvent.NavigateToQueen(queenName))
        }
    }

    fun onAddClick() {
        if (currentState is QueenListUiState.Content) {
            sendEvent(QueenListEvent.NavigateToAddQueen)
        }
    }

    fun resetError() = loadQueens()
}
