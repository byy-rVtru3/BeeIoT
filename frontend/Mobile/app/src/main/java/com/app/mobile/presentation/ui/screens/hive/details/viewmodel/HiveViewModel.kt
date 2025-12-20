package com.app.mobile.presentation.ui.screens.hive.details.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.hives.hive.GetHiveUseCase
import com.app.mobile.presentation.models.hive.QueenUi
import com.app.mobile.presentation.ui.screens.hive.details.HiveRoute
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HiveViewModel(
    savedStateHandle: SavedStateHandle,
    private val getHiveUseCase: GetHiveUseCase
) : ViewModel() {
    private val route = savedStateHandle.toRoute<HiveRoute>()

    private val hiveId = route.hiveId

    private val _hiveUiState = MutableStateFlow<HiveUiState>(HiveUiState.Loading)
    val hiveUiState = _hiveUiState.asStateFlow()

    private val _navigationEvent = Channel<HiveNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    val handler = CoroutineExceptionHandler { _, exception ->
        _hiveUiState.value = HiveUiState.Error(exception.message ?: "Unknown error")
        Log.e("HiveViewModel", "Error loading hive", exception)
    }

    fun loadHive() {
        _hiveUiState.value = HiveUiState.Loading
        viewModelScope.launch(handler) {
            _hiveUiState.value = getHiveUseCase(hiveId)
                ?.let { HiveUiState.Content(it.toUiModel()) }
                ?: HiveUiState.Error("Улей не найден")
        }
    }

    fun onTemperatureClick() =
        navigateWithId(HiveNavigationEvent::NavigateToTemperatureByHive)

    fun onNoiseClick() =
        navigateWithId(HiveNavigationEvent::NavigateToNoiseByHive)

    fun onWeightClick() =
        navigateWithId(HiveNavigationEvent::NavigateToWeightByHive)

    fun onNotificationsClick() =
        navigateWithId(HiveNavigationEvent::NavigateToNotificationByHive)

    fun onQueenClick() {
        val currentUiState = _hiveUiState.value
        if (currentUiState is HiveUiState.Content) {
            val queen = currentUiState.hive.queen
            if (queen is QueenUi.Present) {
                viewModelScope.launch(handler) {
                    _navigationEvent.send(HiveNavigationEvent.NavigateToQueenByHive(queen.queen.id))
                }
            }
        }
    }

    fun onWorksClick() =
        navigateWithId(HiveNavigationEvent::NavigateToWorkByHive)

    fun onHiveListClick() {
        viewModelScope.launch(handler) {
            _navigationEvent.send(HiveNavigationEvent.NavigateToHiveList)
        }
    }

    fun onHiveEditClick() =
        navigateWithId(HiveNavigationEvent::NavigateToHiveEdit)

    private inline fun navigateWithId(crossinline navEvent: (String) -> HiveNavigationEvent) {
        (_hiveUiState.value as? HiveUiState.Content)?.let {
            viewModelScope.launch(handler) {
                _navigationEvent.send(navEvent(hiveId))
            }
        }
    }
}