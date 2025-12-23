package com.app.mobile.presentation.ui.screens.queen.details.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.domain.usecase.hives.hive.GetHivePreviewUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueenUseCase
import com.app.mobile.presentation.mappers.toUiModel
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.queen.details.QueenRoute

class QueenViewModel(
    savedStateHandle: SavedStateHandle,
    private val getQueenUseCase: GetQueenUseCase,
    private val getHivePreviewUseCase: GetHivePreviewUseCase
) : BaseViewModel<QueenUiState, QueenNavigationEvent>(QueenUiState.Loading) {

    private val route = savedStateHandle.toRoute<QueenRoute>()
    private val queenId = route.queenId

    override fun handleError(exception: Throwable) {
        updateState { QueenUiState.Error(exception.message ?: "Unknown error") }
        Log.e("QueenViewModel", exception.message ?: "Unknown error")
    }

    fun getQueen() {
        updateState { QueenUiState.Loading }

        launch {
            val queen = getQueenUseCase(queenId)

            if (queen != null) {
                val hive = queen.hiveId?.let { getHivePreviewUseCase(it) }
                updateState { QueenUiState.Content(queen.toUiModel(hive)) }
            } else {
                updateState { QueenUiState.Error("Матка не найдена") }
            }
        }
    }

    fun onEditQueenClick() {
        val state = currentState
        if (state is QueenUiState.Content) {
            sendEvent(
                QueenNavigationEvent.NavigateToEditQueen(state.queen.id)
            )
        }
    }

    fun onHiveClick() {
        val state = currentState
        if (state is QueenUiState.Content) {
            if (state.queen.hive?.id != null) {
                sendEvent(
                    QueenNavigationEvent.NavigateToHive(state.queen.hive.id)
                )
            }
        }
    }
}