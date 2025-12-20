package com.app.mobile.presentation.ui.screens.queen.details.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.mobile.domain.usecase.hives.hive.GetHivePreviewUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueenUseCase
import com.app.mobile.presentation.mappers.toUiModel
import com.app.mobile.presentation.ui.screens.queen.details.QueenRoute
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class QueenViewModel(
    savedStateHandle: SavedStateHandle,
    private val getQueenUseCase: GetQueenUseCase,
    private val getHivePreviewUseCase: GetHivePreviewUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<QueenRoute>()
    private val queenId = route.queenId

    private val _queenUiState = MutableStateFlow<QueenUiState>(QueenUiState.Loading)
    val queenUiState = _queenUiState.asStateFlow()

    private val _navigationEvent = Channel<QueenNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    val handler = CoroutineExceptionHandler { _, exception ->
        _queenUiState.value = QueenUiState.Error(exception.message ?: "Unknown error")
        Log.e("QueenViewModel", exception.message ?: "Unknown error")
    }

    fun getQueen() {
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
            viewModelScope.launch(handler) {
                _navigationEvent.send(
                    QueenNavigationEvent.NavigateToEditQueen(currentUiState.queen.id)
                )
            }
        }
    }

    fun onHiveClick() {
        val currentUiState = _queenUiState.value
        if (currentUiState is QueenUiState.Content) {
            if (currentUiState.queen.hive?.id != null) {
                viewModelScope.launch(handler) {
                    _navigationEvent.send(
                        QueenNavigationEvent.NavigateToHive(currentUiState.queen.hive.id)
                    )
                }
            }
        }
    }
}