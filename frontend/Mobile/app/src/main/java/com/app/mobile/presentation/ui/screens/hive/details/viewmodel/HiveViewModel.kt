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
) : BaseViewModel<HiveUiState, HiveNavigationEvent>(HiveUiState.Loading) {
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
            updateState {
                hive
                    ?.let { HiveUiState.Content(it.toUiModel()) }
                    ?: HiveUiState.Error("Улей не найден")
            }
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
        val state = currentState
        if (state is HiveUiState.Content) {
            val queen = state.hive.queen
            if (queen is QueenUi.Present) {
                launch {
                    sendEvent(HiveNavigationEvent.NavigateToQueenByHive(queen.queen.id))
                }
            }
        }
    }

    fun onWorksClick() =
        navigateWithId(HiveNavigationEvent::NavigateToWorkByHive)

    fun onHiveListClick() {
        launch {
            sendEvent(HiveNavigationEvent.NavigateToHiveList)
        }
    }

    fun onHiveEditClick() =
        navigateWithId(HiveNavigationEvent::NavigateToHiveEdit)

    private inline fun navigateWithId(crossinline navEvent: (String) -> HiveNavigationEvent) {
        (currentState as? HiveUiState.Content)?.let {
            launch {
                sendEvent(navEvent(hiveId))
            }
        }
    }
}