package com.app.mobile.presentation.ui.screens.queen.list.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.usecase.hives.queen.GetQueensUseCase
import com.app.mobile.presentation.mappers.toPreviewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class QueenListViewModel(
    private val getQueensUseCase: GetQueensUseCase
) : ViewModel() {

    private val _queenListUiState = MutableLiveData<QueenListUiState>(QueenListUiState.Loading)
    val queenListUiState: LiveData<QueenListUiState> = _queenListUiState

    private val _navigationEvent = MutableLiveData<QueenListNavigationEvent?>()
    val navigationEvent: LiveData<QueenListNavigationEvent?> = _navigationEvent

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
            _navigationEvent.value = QueenListNavigationEvent.NavigateToQueen(queenId)
        }
    }

    fun onAddClick() {
        val currentState = _queenListUiState.value
        if (currentState is QueenListUiState.Content) {
            _navigationEvent.value = QueenListNavigationEvent.NavigateToAddQueen
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}