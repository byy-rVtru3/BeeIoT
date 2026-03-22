package com.app.mobile.presentation.ui.screens.hive.details.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.mappers.toPreviewModel
import com.app.mobile.domain.mappers.toUiModel
import com.app.mobile.domain.scenario.GetHiveScenario
import com.app.mobile.domain.usecase.hives.works.GetWorksUseCase
import com.app.mobile.presentation.models.hive.HiveUi
import com.app.mobile.presentation.ui.components.BaseViewModel
import com.app.mobile.presentation.ui.screens.hive.details.HiveRoute
import kotlinx.coroutines.async

class HiveViewModel(
	savedStateHandle: SavedStateHandle,
	private val getHiveScenario: GetHiveScenario,
	private val getWorksUseCase: GetWorksUseCase,
) : BaseViewModel<HiveUiState, HiveEvent>(HiveUiState.Loading) {

	private val route = savedStateHandle.toRoute<HiveRoute>()

	private val hiveName = route.hiveName

	override fun handleError(exception: Throwable) {
		updateState { HiveUiState.Error(exception.message ?: "Unknown error") }
		Log.e("HiveViewModel", "Error loading hive", exception)
	}

	fun loadHive() {
		updateState { HiveUiState.Loading }
		launch {
			val hiveDeferred = async { getHiveScenario(hiveName) }
			val worksDeferred = async { getWorksUseCase(hiveName) }

			when (val result = hiveDeferred.await()) {
				is ApiResult.Success -> {
					val hive = result.data
					val recentWorks = worksDeferred.await()
						.sortedByDescending { it.dateTime }
						.take(2)
						.map { it.toUiModel() }

					updateState {
						HiveUiState.Content(
							HiveUi(
								name = hive.name,
								hub = hive.hub?.toUiModel(),
								queen = hive.queen?.toPreviewModel(),
								recentWorks = recentWorks,
							)
						)
					}
				}

				else -> {
					sendEvent(HiveEvent.ShowSnackBar(result.toErrorMessage()))
					sendEvent(HiveEvent.NavigateToHiveList)
				}
			}
		}
	}

	fun resetError() = loadHive()

	fun onTemperatureClick() =
		navigateWithName(HiveEvent::NavigateToTemperatureByHive)

	fun onNoiseClick() =
		navigateWithName(HiveEvent::NavigateToNoiseByHive)

	fun onWeightClick() =
		navigateWithName(HiveEvent::NavigateToWeightByHive)

	fun onNotificationsClick() =
		navigateWithName(HiveEvent::NavigateToNotificationByHive)

	fun onQueenClick() {
		val state = currentState
		if (state is HiveUiState.Content) {
			val queenName = state.hive.queen?.name
			if (queenName != null) {
				launch {
					sendEvent(HiveEvent.NavigateToQueenByHive(queenName))
				}
			}
		}
	}

	fun onWorksClick() =
		navigateWithName(HiveEvent::NavigateToWorkByHive)

	fun onWorkClick(workId: String) {
		launch {
			sendEvent(HiveEvent.NavigateToWorkDetail(workId, hiveName))
		}
	}

	fun onHiveListClick() {
		launch {
			sendEvent(HiveEvent.NavigateToHiveList)
		}
	}

	fun onHiveEditClick() =
		navigateWithName(HiveEvent::NavigateToHiveEdit)

	private inline fun navigateWithName(crossinline navEvent: (String) -> HiveEvent) {
		(currentState as? HiveUiState.Content)?.let {
			launch {
				sendEvent(navEvent(hiveName))
			}
		}
	}
}
