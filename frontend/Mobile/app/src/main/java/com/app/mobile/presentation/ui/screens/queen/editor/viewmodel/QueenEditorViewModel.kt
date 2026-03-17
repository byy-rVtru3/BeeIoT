package com.app.mobile.presentation.ui.screens.queen.editor.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.usecase.hives.queen.CreateQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.SaveQueenUseCase
import com.app.mobile.presentation.mappers.toPresentation
import com.app.mobile.presentation.mappers.toEditor
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.queen.editor.QueenEditorRoute
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class QueenEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val createQueenUseCase: CreateQueenUseCase,
    private val getQueenUseCase: GetQueenUseCase,
    private val saveQueenUseCase: SaveQueenUseCase
) : BaseViewModel<QueenEditorUiState, QueenEditorEvent>(QueenEditorUiState.Loading) {

    private val route = savedStateHandle.toRoute<QueenEditorRoute>()
    private val queenName = route.queenName
    private val isNew = queenName == null

    override fun handleError(exception: Throwable) {
        updateState { QueenEditorUiState.Error(exception.message ?: "Unknown error") }
        Log.e("QueenEditorViewModel", exception.message ?: "Unknown error")
    }

    fun loadQueen() {
        updateState { QueenEditorUiState.Loading }
        launch {
            if (queenName != null) {
                when (val result = getQueenUseCase(queenName)) {
                    is ApiResult.Success -> {
                        val uiModel = result.data.toEditor()
                        updateState { QueenEditorUiState.Content(uiModel) }
                    }

                    else -> {
                        updateState { QueenEditorUiState.Error(result.toErrorMessage()) }
                    }
                }
            } else {
                val uiModel = createQueenUseCase().toPresentation()
                updateState { QueenEditorUiState.Content(uiModel) }
            }
        }
    }

    fun onNameChange(name: String) {
        val state = currentState
        if (state is QueenEditorUiState.Content) {
            val updatedQueen = state.queenEditorModel.copy(name = name)
            updateState { QueenEditorUiState.Content(updatedQueen) }
        }
    }

    fun onDateChange(birthDate: Long) {
        val state = currentState
        if (state is QueenEditorUiState.Content) {
            val updatedQueen = state.queenEditorModel.copy(birthDate = birthDate)
            updateState { QueenEditorUiState.Content(updatedQueen) }
        }
    }

    fun resetError() = loadQueen()

    fun onSaveClick() {
        val state = currentState
        if (state is QueenEditorUiState.Content) {
            launch {
                updateState { QueenEditorUiState.Loading }
                val name = state.queenEditorModel.name
                val startDate = Instant
                    .ofEpochMilli(state.queenEditorModel.birthDate)
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate()

                when (val result = saveQueenUseCase(name, startDate)) {
                    is ApiResult.Success -> {
                        sendEvent(QueenEditorEvent.NavigateBack)
                    }

                    else -> {
                        sendEvent(QueenEditorEvent.ShowSnackBar(result.toErrorMessage()))
                        updateState { QueenEditorUiState.Content(state.queenEditorModel) }
                    }
                }
            }
        }
    }
}
