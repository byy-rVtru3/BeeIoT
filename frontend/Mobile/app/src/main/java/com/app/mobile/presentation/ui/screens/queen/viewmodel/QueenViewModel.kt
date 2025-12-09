package com.app.mobile.presentation.ui.screens.queen.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.hives.GetHivePreviewUseCase
import com.app.mobile.domain.usecase.hives.GetQueenUseCase
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

    fun getQueen(hiveId: Int) {
        _queenUiState.value = QueenUiState.Loading
        viewModelScope.launch(handler) {
            val hive = getHivePreviewUseCase(hiveId)
            hive?.let {
                _queenUiState.value = getQueenUseCase(hiveId)
                    ?.let { QueenUiState.Content(it.toUiModel(hive.name)) }
                    ?: QueenUiState.Error("Матка не найдена")
            } ?: QueenUiState.Error("Улей не найден")
        }
    }

    fun onEditQueenClick() {
        val currentUiState = _queenUiState.value
        if (currentUiState is QueenUiState.Content) {
            _navigationEvent.value =
                QueenNavigationEvent.NavigateToEditQueen(currentUiState.queen.hiveId)
        }
    }
}