package com.app.mobile.presentation.ui.screens.queen.editor.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.models.hives.queen.QueenCalendarRequestResult
import com.app.mobile.domain.usecase.hives.hive.GetHivesPreviewUseCase
import com.app.mobile.domain.usecase.hives.queen.CalcQueenCalendarUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.CreateQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.SaveQueenUseCase
import com.app.mobile.presentation.mappers.toDomain
import com.app.mobile.presentation.mappers.toEditor
import com.app.mobile.presentation.mappers.toPresentation
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class QueenEditorViewModel(
    private val createQueenUseCase: CreateQueenUseCase,
    private val getQueenUseCase: GetQueenUseCase,
    private val getHivesPreviewUseCase: GetHivesPreviewUseCase,
    private val calcQueenCalendarUseCase: CalcQueenCalendarUseCase,
    private val saveQueenUseCase: SaveQueenUseCase
) : ViewModel() {
    private val _queenEditorUiState =
        MutableLiveData<QueenEditorUiState>(QueenEditorUiState.Loading)
    val queenEditorUiState: LiveData<QueenEditorUiState> = _queenEditorUiState

    private val _navigationEvent = MutableLiveData<QueenEditorNavigationEvent?>()
    val navigationEvent: LiveData<QueenEditorNavigationEvent?> = _navigationEvent

    val handler = CoroutineExceptionHandler { _, exception ->
        _queenEditorUiState.value = QueenEditorUiState.Error(exception.message ?: "Unknown error")
        Log.e("QueenEditorViewModel", exception.message ?: "Unknown error")
    }

    fun loadQueen(queenId: String?) {
        _queenEditorUiState.value = QueenEditorUiState.Loading
        viewModelScope.launch(handler) {

            coroutineScope {
                val hivesDeferred = async { getHivesPreviewUseCase() }

                val queenDeferred = async {
                    if (queenId != null) getQueenUseCase(queenId) else null
                }

                val hives = hivesDeferred.await()
                val foundQueen = queenDeferred.await()

                val uiModel =
                    foundQueen?.toEditor(hives) ?: createQueenUseCase().toPresentation(hives)

                _queenEditorUiState.value = QueenEditorUiState.Content(uiModel)
            }
        }
    }

    fun onNameChange(name: String) {
        val currentState = _queenEditorUiState.value
        if (currentState is QueenEditorUiState.Content) {
            val updatedQueen = currentState.queenEditorModel.copy(name = name)
            _queenEditorUiState.value = QueenEditorUiState.Content(updatedQueen)
        }
    }

    fun onDateChange(birthDate: Long) {
        val currentState = _queenEditorUiState.value
        if (currentState is QueenEditorUiState.Content) {
            val updatedQueen = currentState.queenEditorModel.copy(birthDate = birthDate)
            _queenEditorUiState.value = QueenEditorUiState.Content(updatedQueen)
        }
    }

    fun addHive(hiveId: String) {
        val currentState = _queenEditorUiState.value
        if (currentState is QueenEditorUiState.Content) {
            val updatedQueen = currentState.queenEditorModel.copy(hiveId = hiveId)
            _queenEditorUiState.value = QueenEditorUiState.Content(updatedQueen)
        }
    }

    fun onSaveClick() {
        val currentState = _queenEditorUiState.value
        if (currentState is QueenEditorUiState.Content) {
            viewModelScope.launch(handler) {
                val result = calcQueenCalendarUseCase(currentState.queenEditorModel.toDomain())
                when (result) {
                    is QueenCalendarRequestResult.Success -> {
                        saveQueenUseCase(
                            currentState.queenEditorModel
                                .toDomain() // так себе но пойдет
                                .toDomain(result.queenLifecycle)
                        )
                        _navigationEvent.value = QueenEditorNavigationEvent.NavigateBack
                    }

                    is QueenCalendarRequestResult.Error -> {
                        _queenEditorUiState.value = QueenEditorUiState.Error(result.message)
                    }
                }

            }
        }
    }

    fun onNavigationHandled() {
        _navigationEvent.value = null
    }
}
