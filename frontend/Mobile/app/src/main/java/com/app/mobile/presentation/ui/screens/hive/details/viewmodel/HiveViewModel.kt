package com.app.mobile.presentation.ui.screens.hive.details.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.usecase.hives.hive.GetHiveUseCase
import com.app.mobile.presentation.models.hive.QueenUi
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.hive.details.HiveRoute

class HiveViewModel(
    savedStateHandle: SavedStateHandle,
    private val getHiveUseCase: GetHiveUseCase
) : BaseViewModel<HiveUiState, HiveEvent>(HiveUiState.Loading) {
    private val route = savedStateHandle.toRoute<HiveRoute>()

    private val hiveId = route.hiveId

    override fun handleError(exception: Throwable) {
        updateState { HiveUiState.Error(exception.message ?: "Unknown error") }
        Log.e("HiveViewModel", "Error loading hive", exception)
    }

    fun loadHive() {
        updateState { HiveUiState.Loading }
        launch {
            val hive = getHiveUseCase(hiveId)
            if (hive == null) {
                sendEvent(HiveEvent.ShowSnackBar("Улей не найден"))
                sendEvent(HiveEvent.NavigateToHiveList)
                return@launch
            }
            updateState {
                hive.let { HiveUiState.Content(it.toUiModel()) }
            }
        }
    }

    fun resetError() = loadHive()

    fun onTemperatureClick() =
        navigateWithId(HiveEvent::NavigateToTemperatureByHive)

    fun onNoiseClick() =
        navigateWithId(HiveEvent::NavigateToNoiseByHive)

    fun onWeightClick() =
        navigateWithId(HiveEvent::NavigateToWeightByHive)

    fun onNotificationsClick() =
        navigateWithId(HiveEvent::NavigateToNotificationByHive)

    fun onQueenClick() {
        val state = currentState
        if (state is HiveUiState.Content) {
            val queen = state.hive.queen
            if (queen is QueenUi.Present) {
                launch {
                    sendEvent(HiveEvent.NavigateToQueenByHive(queen.queen.id))
                }
            }
        }
    }

    fun onWorksClick() =
        navigateWithId(HiveEvent::NavigateToWorkByHive)

    fun onHiveListClick() {
        launch {
            sendEvent(HiveEvent.NavigateToHiveList)
        }
    }

    fun onHiveEditClick() =
        navigateWithId(HiveEvent::NavigateToHiveEdit)

    private inline fun navigateWithId(crossinline navEvent: (String) -> HiveEvent) {
        (currentState as? HiveUiState.Content)?.let {
            launch {
                sendEvent(navEvent(hiveId))
            }
        }
    }
}