package com.app.mobile.presentation.ui.screens.queen.editor.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.models.hives.queen.QueenCalendarRequestResult
import com.app.mobile.domain.usecase.hives.hive.GetHivesPreviewUseCase
import com.app.mobile.domain.usecase.hives.queen.CalcQueenCalendarUseCase
import com.app.mobile.domain.usecase.hives.queen.CreateQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.SaveQueenUseCase
import com.app.mobile.presentation.mappers.toDomain
import com.app.mobile.presentation.mappers.toEditor
import com.app.mobile.presentation.mappers.toPresentation
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.queen.editor.QueenEditorRoute
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class QueenEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val createQueenUseCase: CreateQueenUseCase,
    private val getQueenUseCase: GetQueenUseCase,
    private val getHivesPreviewUseCase: GetHivesPreviewUseCase,
    private val calcQueenCalendarUseCase: CalcQueenCalendarUseCase,
    private val saveQueenUseCase: SaveQueenUseCase
) : BaseViewModel<QueenEditorUiState, QueenEditorNavigationEvent>(QueenEditorUiState.Loading) {

    private val route = savedStateHandle.toRoute<QueenEditorRoute>()
    private val queenId = route.queenId

    override fun handleError(exception: Throwable) {
        updateState { QueenEditorUiState.Error(exception.message ?: "Unknown error") }
        Log.e("QueenEditorViewModel", exception.message ?: "Unknown error")
    }

    fun loadQueen() {
        updateState { QueenEditorUiState.Loading }
        launch {
            coroutineScope {
                val hivesDeferred = async { getHivesPreviewUseCase() }

                val queenDeferred = async {
                    if (queenId != null) getQueenUseCase(queenId) else null
                }

                val hives = hivesDeferred.await()
                val foundQueen = queenDeferred.await()

                val uiModel =
                    foundQueen?.toEditor(hives) ?: createQueenUseCase().toPresentation(hives)

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

    fun addHive(hiveId: String) {
        val state = currentState
        if (state is QueenEditorUiState.Content) {
            val updatedQueen = state.queenEditorModel.copy(hiveId = hiveId)
            updateState { QueenEditorUiState.Content(updatedQueen) }
        }
    }

    fun onSaveClick() {
        val state = currentState
        if (state is QueenEditorUiState.Content) {
            launch {
                when (val result = calcQueenCalendarUseCase(state.queenEditorModel.toDomain())) {
                    is QueenCalendarRequestResult.Success -> {
                        saveQueenUseCase(
                            state.queenEditorModel
                                .toDomain() // так себе но пойдет
                                .toDomain(result.queenLifecycle)
                        )
                        sendEvent(QueenEditorNavigationEvent.NavigateBack)
                    }

                    is QueenCalendarRequestResult.Error -> {
                        updateState { QueenEditorUiState.Error(result.message) }
                    }
                }

            }
        }
    }

}
