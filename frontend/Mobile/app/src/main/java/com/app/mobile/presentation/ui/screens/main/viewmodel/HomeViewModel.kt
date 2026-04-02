package com.app.mobile.presentation.ui.screens.main.viewmodel

import android.util.Log
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.usecase.hives.hive.GetHivesPreviewUseCase
import com.app.mobile.domain.usecase.hives.hub.GetHubsUseCase
import com.app.mobile.domain.usecase.hives.queen.GetQueensUseCase
import com.app.mobile.domain.usecase.hives.works.GetWorksUseCase
import com.app.mobile.domain.usecase.notifications.CheckIfNotificationPromptShownUseCase
import com.app.mobile.domain.usecase.notifications.SendPushTokenUseCase
import com.app.mobile.domain.usecase.notifications.SetNotificationPromptShownUseCase
import com.app.mobile.presentation.ui.components.BaseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class HomeViewModel(
	private val getHivesPreviewUseCase: GetHivesPreviewUseCase,
	private val getQueensUseCase: GetQueensUseCase,
	private val getHubsUseCase: GetHubsUseCase,
	private val getWorksUseCase: GetWorksUseCase,
	private val checkIfNotificationPromptShownUseCase: CheckIfNotificationPromptShownUseCase,
	private val setNotificationPromptShownUseCase: SetNotificationPromptShownUseCase,
	private val sendPushTokenUseCase: SendPushTokenUseCase
) : BaseViewModel<HomeUiState, HomeEvent>(HomeUiState.Loading) {

	override fun handleError(exception: Throwable) {
		updateState { HomeUiState.Error(exception.message ?: "Неизвестная ошибка") }
		Log.e("HomeViewModel", exception.message.toString())
	}

	fun loadData() {
		updateState { HomeUiState.Loading }
		fetchData()
	}

	fun refresh() {
		val current = currentState as? HomeUiState.Content ?: return
		updateState { current.copy(isRefreshing = true) }
		fetchData()
	}

	private fun fetchData() {
		launch {
			val hivesDeferred = async { getHivesPreviewUseCase() }
			val queensDeferred = async { getQueensUseCase() }
			val hubsDeferred = async { getHubsUseCase() }

			val hivesResult = hivesDeferred.await()
			val queensResult = queensDeferred.await()
			val hubsResult = hubsDeferred.await()

			if (hivesResult is ApiResult.Success &&
				queensResult is ApiResult.Success &&
				hubsResult is ApiResult.Success
			) {
				val hives = hivesResult.data
				val queens = queensResult.data
				val hubs = hubsResult.data

				val allWorks = hives
					.map { hive -> async { getWorksUseCase(hive.name) } }
					.awaitAll()
					.mapNotNull { (it as? ApiResult.Success)?.data }
					.flatten()
					.sortedByDescending { it.dateTime }

				updateState { HomeUiState.Content(hives, queens, hubs, allWorks) }
				val shouldShowPrompt = !checkIfNotificationPromptShownUseCase()
				val contentState = currentState as? HomeUiState.Content ?: return@launch
				if (shouldShowPrompt) {
					updateState { contentState.copy(showNotificationPrompt = true) }
				}
			} else {
				val errorResult = listOf(hivesResult, queensResult, hubsResult)
					.firstOrNull { it !is ApiResult.Success }
				updateState { HomeUiState.Error(errorResult?.toErrorMessage() ?: "Ошибка загрузки") }
			}
		}
	}

	fun onRetry() = loadData()

	fun onHiveClick(hiveName: String) {
		sendEvent(HomeEvent.NavigateToHive(hiveName))
	}

	fun onQueenClick(queenName: String) {
		sendEvent(HomeEvent.NavigateToQueen(queenName))
	}

	fun onHubClick(hubId: String) {
		sendEvent(HomeEvent.NavigateToHub(hubId))
	}

	fun onWorkClick(workId: String, hiveId: String) {
		sendEvent(HomeEvent.NavigateToWork(workId, hiveId))
	}

	fun onAcceptNotificationPrompt() {
		val state = currentState as? HomeUiState.Content ?: return
		updateState { state.copy(showNotificationPrompt = false) }
		launch {
			setNotificationPromptShownUseCase(true)
			sendPushTokenUseCase()
		}
	}

	fun onDeclineNotificationPrompt() {
		val state = currentState as? HomeUiState.Content ?: return
		updateState { state.copy(showNotificationPrompt = false) }
		launch {
			setNotificationPromptShownUseCase(false)
			sendPushTokenUseCase()
		}
	}
}
