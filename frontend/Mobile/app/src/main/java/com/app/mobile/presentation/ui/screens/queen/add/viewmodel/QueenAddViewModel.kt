package com.app.mobile.presentation.ui.screens.queen.add.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.mobile.domain.usecase.hives.GetHivesPreviewUseCase
import com.app.mobile.domain.usecase.hives.queen.CreateQueenUseCase
import com.app.mobile.domain.usecase.hives.queen.SaveQueenUseCase
import com.app.mobile.presentation.mappers.toDomain
import com.app.mobile.presentation.mappers.toPresentation
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

class QueenAddViewModel(
    private val createQueenUseCase: CreateQueenUseCase,
    private val getHivesPreviewUseCase: GetHivesPreviewUseCase,
    private val saveQueenUseCase: SaveQueenUseCase
) : ViewModel() {
    private val _queenAddUiState = MutableLiveData<QueenAddUiState>(QueenAddUiState.Loading)
    val queenAddUiState: LiveData<QueenAddUiState> = _queenAddUiState

    private val _navigationEvent = MutableLiveData<QueenAddNavigationEvent?>()
    val navigationEvent: LiveData<QueenAddNavigationEvent?> = _navigationEvent

    val handler = CoroutineExceptionHandler { _, exception ->
        _queenAddUiState.value = QueenAddUiState.Error(exception.message ?: "Unknown error")
        Log.e("QueenAddViewModel", exception.message ?: "Unknown error")
    }

    fun createQueen() {
        _queenAddUiState.value = QueenAddUiState.Loading
        viewModelScope.launch(handler) {
            val hives = getHivesPreviewUseCase()
            val queen = createQueenUseCase().toPresentation(hives)
            _queenAddUiState.value = QueenAddUiState.Content(queen)
        }
    }

    fun onNameChange(name: String) {
        val currentState = _queenAddUiState.value
        if (currentState is QueenAddUiState.Content) {
            val updatedQueen = currentState.queenAddModel.copy(name = name)
            _queenAddUiState.value = QueenAddUiState.Content(updatedQueen)
        }
    }

    fun onDateChange(birthDate: Long) {
        val currentState = _queenAddUiState.value
        if (currentState is QueenAddUiState.Content) {
            val updatedQueen = currentState.queenAddModel.copy(birthDate = birthDate)
            _queenAddUiState.value = QueenAddUiState.Content(updatedQueen)
        }
    }

    fun addHive(hiveId: String) {
        val currentState = _queenAddUiState.value
        if (currentState is QueenAddUiState.Content) {
            val updatedQueen = currentState.queenAddModel.copy(hiveId = hiveId)
            _queenAddUiState.value = QueenAddUiState.Content(updatedQueen)
        }
    }

    fun onSaveClick() {
        val currentState = _queenAddUiState.value
        if (currentState is QueenAddUiState.Content) {
            viewModelScope.launch(handler) {
                val result = saveQueenUseCase(currentState.queenAddModel.toDomain())
                result
                    .onSuccess {
                        _navigationEvent.value = QueenAddNavigationEvent.NavigateBack
                    }
                    .onFailure { exception ->
                        _queenAddUiState.value =
                            QueenAddUiState.Error(exception.message ?: "Unknown error")
                    }
            }
        }
    }
}
