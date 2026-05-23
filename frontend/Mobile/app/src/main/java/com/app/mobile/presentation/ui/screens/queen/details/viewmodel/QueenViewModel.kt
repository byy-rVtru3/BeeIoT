package com.app.mobile.presentation.ui.screens.queen.details.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.usecase.hives.queen.DeleteQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueenUseCase
import com.app.mobile.presentation.mappers.toUiModel
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.queen.details.QueenRoute

class QueenViewModel(
    savedStateHandle: SavedStateHandle,
    private val getQueenUseCase: GetQueenUseCase,
    private val deleteQueenUseCase: DeleteQueenUseCase,
) : BaseViewModel<QueenUiState, QueenEvent>(QueenUiState.Loading) {

    private val route = savedStateHandle.toRoute<QueenRoute>()
    private val queenName = route.queenName
    private val fromHiveName = route.fromHiveName

    override fun handleError(exception: Throwable) {
        updateState { QueenUiState.Error(exception.message ?: "Unknown error") }
        Log.e("QueenViewModel", exception.message ?: "Unknown error")
    }

    fun getQueen() {
        updateState { QueenUiState.Loading }

        launch {
            when (val result = getQueenUseCase(queenName)) {
                is ApiResult.Success -> {
                    updateState { QueenUiState.Content(result.data.toUiModel(), fromHiveName) }
                }

                else -> {
                    sendEvent(QueenEvent.ShowSnackBar(result.toErrorMessage()))
                    sendEvent(QueenEvent.NavigateBack)
                }
            }
        }
    }

    fun refresh() {
        val current = currentState as? QueenUiState.Content ?: return
        updateState { current.copy(isRefreshing = true) }
        launch {
            when (val result = getQueenUseCase(queenName)) {
                is ApiResult.Success -> {
                    updateState { QueenUiState.Content(result.data.toUiModel(), fromHiveName) }
                }
                else -> {
                    updateState { current.copy(isRefreshing = false) }
                    sendEvent(QueenEvent.ShowSnackBar(result.toErrorMessage()))
                }
            }
        }
    }

    fun resetError() = getQueen()

    fun onEditQueenClick() {
        val state = currentState
        if (state is QueenUiState.Content) {
            sendEvent(
                QueenEvent.NavigateToEditQueen(state.queen.name)
            )
        }
    }

    fun onDeleteClick() {
        launch {
            when (val result = deleteQueenUseCase(queenName)) {
                is ApiResult.Success -> sendEvent(QueenEvent.NavigateBack)
                else -> sendEvent(QueenEvent.ShowSnackBar(result.toErrorMessage()))
            }
        }
    }

    fun onHiveClick() {
        val hiveName = fromHiveName ?: return
        launch { sendEvent(QueenEvent.NavigateToHive(hiveName)) }
    }
}
