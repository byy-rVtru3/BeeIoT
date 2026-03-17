package com.app.mobile.presentation.ui.screens.queen.details.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.usecase.hives.queen.GetQueenUseCase
import com.app.mobile.presentation.mappers.toUiModel
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.queen.details.QueenRoute

class QueenViewModel(
    savedStateHandle: SavedStateHandle,
    private val getQueenUseCase: GetQueenUseCase
) : BaseViewModel<QueenUiState, QueenEvent>(QueenUiState.Loading) {

    private val route = savedStateHandle.toRoute<QueenRoute>()
    private val queenName = route.queenName

    override fun handleError(exception: Throwable) {
        updateState { QueenUiState.Error(exception.message ?: "Unknown error") }
        Log.e("QueenViewModel", exception.message ?: "Unknown error")
    }

    fun getQueen() {
        updateState { QueenUiState.Loading }

        launch {
            when (val result = getQueenUseCase(queenName)) {
                is ApiResult.Success -> {
                    updateState { QueenUiState.Content(result.data.toUiModel()) }
                }

                else -> {
                    sendEvent(QueenEvent.ShowSnackBar(result.toErrorMessage()))
                    sendEvent(QueenEvent.NavigateBack)
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

    fun onHiveClick() {
        // Hive navigation from queen details is no longer supported
        // since queen no longer knows its hive
    }
}
