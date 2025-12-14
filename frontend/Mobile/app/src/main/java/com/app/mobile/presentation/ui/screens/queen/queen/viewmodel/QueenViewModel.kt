package com.app.mobile.presentation.ui.screens.queen.queen.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.usecase.hives.GetHivePreviewUseCase
import com.app.mobile.domain.usecase.hives.GetQueenUseCase
import com.app.mobile.presentation.mappers.toUiModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class QueenViewModel(
    private val getQueenUseCase: GetQueenUseCase,
    private val getHivePreviewUseCase: GetHivePreviewUseCase
) : ViewModel() {
    private val _queenUiState = MutableLiveData<QueenUiState>(QueenUiState.Loading)
    val queenUiState: LiveData<QueenUiState> = _queenUiState

    private val _navigationEvent = MutableLiveData<QueenNavigationEvent?>()
    val navigationEvent: LiveData<QueenNavigationEvent?> = _navigationEvent

    val handler = CoroutineExceptionHandler { _, exception ->
        _queenUiState.value = QueenUiState.Error(exception.message ?: "Unknown error")
        Log.e("QueenViewModel", exception.message ?: "Unknown error")
    }

    fun getQueen(queenId: String) {
        _queenUiState.value = QueenUiState.Loading

        viewModelScope.launch(handler) {
            val queen = getQueenUseCase(queenId)

            _queenUiState.value = if (queen != null) {
                val hive = queen.hiveId?.let { getHivePreviewUseCase(it) }
                QueenUiState.Content(queen.toUiModel(hive))
            } else {
                QueenUiState.Error("Матка не найдена")
            }
        }
    }

    fun onEditQueenClick() {
        val currentUiState = _queenUiState.value
        if (currentUiState is QueenUiState.Content) {
            _navigationEvent.value =
                QueenNavigationEvent.NavigateToEditQueen(currentUiState.queen.id)
        }
    }
}