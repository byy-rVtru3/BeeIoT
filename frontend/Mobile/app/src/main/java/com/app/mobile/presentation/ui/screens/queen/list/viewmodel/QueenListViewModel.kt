package com.app.mobile.presentation.ui.screens.queen.list.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.usecase.hives.queen.GetQueensUseCase
import com.app.mobile.presentation.mappers.toPreviewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class QueenListViewModel(
    private val getQueensUseCase: GetQueensUseCase
) : ViewModel() {

    private val _queenListUiState = MutableStateFlow<QueenListUiState>(QueenListUiState.Loading)
    val queenListUiState = _queenListUiState.asStateFlow()

    private val _navigationEvent = Channel<QueenListNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    val handler = CoroutineExceptionHandler { _, exception ->
        _queenListUiState.value = QueenListUiState.Error(exception.message ?: "Unknown error")
        Log.e("QueenListViewModel", exception.message ?: "Unknown error")
    }

    fun loadQueens() {
        _queenListUiState.value = QueenListUiState.Loading
        viewModelScope.launch(handler) {
            val queens = getQueensUseCase().map { it.toPreviewModel() }

            _queenListUiState.value = QueenListUiState.Content(queens)
        }
    }

    fun onQueenClick(queenId: String) {
        val currentState = _queenListUiState.value
        if (currentState is QueenListUiState.Content) {
            viewModelScope.launch(handler) {
                _navigationEvent.send(QueenListNavigationEvent.NavigateToQueen(queenId))
            }
        }
    }

    fun onAddClick() {
        val currentState = _queenListUiState.value
        if (currentState is QueenListUiState.Content) {
            viewModelScope.launch(handler) {
                _navigationEvent.send(QueenListNavigationEvent.NavigateToAddQueen)
            }
        }
    }
}