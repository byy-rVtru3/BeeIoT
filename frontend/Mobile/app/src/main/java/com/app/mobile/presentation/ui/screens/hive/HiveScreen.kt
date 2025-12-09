package com.app.mobile.presentation.ui.screens.hive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.app.mobile.presentation.models.hive.HiveUi
import com.app.mobile.presentation.models.hive.HubUi
import com.app.mobile.presentation.models.hive.NotificationUi
import com.app.mobile.presentation.models.hive.QueenUi
import com.app.mobile.presentation.models.hive.WorkUi
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.screens.hive.models.HiveActions
import com.app.mobile.presentation.ui.screens.hive.viewmodel.HiveNavigationEvent
import com.app.mobile.presentation.ui.screens.hive.viewmodel.HiveUiState
import com.app.mobile.presentation.ui.screens.hive.viewmodel.HiveViewModel

@Composable
fun HiveScreen(
    hiveViewModel: HiveViewModel,
    hiveId: Int,
    onQueenClick: (hiveId: Int) -> Unit,
    onWorksClick: (hiveId: Int) -> Unit,
    onNotificationsClick: (hiveId: Int) -> Unit,
    onTemperatureClick: (hiveId: Int) -> Unit,
    onNoiseClick: (hiveId: Int) -> Unit,
    onWeightClick: (hiveId: Int) -> Unit,
    onHiveListClick: () -> Unit,
    onHiveEditClick: (hiveId: Int) -> Unit
) {
    val hiveUiState by hiveViewModel.hiveUiState.observeAsState(HiveUiState.Loading)
    val navigationEvent by hiveViewModel.navigationEvent.observeAsState()

    LaunchedEffect(hiveId) {
        hiveViewModel.loadHive(hiveId)
    }

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { event ->
            when (event) {
                is HiveNavigationEvent.NavigateToHiveList -> onHiveListClick()
                is HiveNavigationEvent.NavigateToQueenByHive -> onQueenClick(event.hiveId)
                is HiveNavigationEvent.NavigateToWorkByHive -> onWorksClick(event.hiveId)
                is HiveNavigationEvent.NavigateToNotificationByHive -> onNotificationsClick(event.hiveId)
                is HiveNavigationEvent.NavigateToTemperatureByHive -> onTemperatureClick(event.hiveId)
                is HiveNavigationEvent.NavigateToNoiseByHive -> onNoiseClick(event.hiveId)
                is HiveNavigationEvent.NavigateToWeightByHive -> onWeightClick(event.hiveId)
                is HiveNavigationEvent.NavigateToHiveEdit -> onHiveEditClick(event.hiveId)
            }
            hiveViewModel.onNavigationHandled()
        }
    }

    when (val state = hiveUiState) {
        is HiveUiState.Loading -> FullScreenProgressIndicator()

        is HiveUiState.Error -> ErrorMessage(
            message = state.message,
            onRetry = {}
        )

        is HiveUiState.Content -> {
            val actions = HiveActions(
                onQueenClick = hiveViewModel::onQueenClick,
                onWorkClick = hiveViewModel::onWorksClick,
                onNotificationClick = hiveViewModel::onNotificationsClick,
                onTemperatureClick = hiveViewModel::onTemperatureClick,
                onNoiseClick = hiveViewModel::onNoiseClick,
                onWeightClick = hiveViewModel::onWeightClick,
                onHiveListClick = hiveViewModel::onHiveListClick,
                onHiveEditClick = hiveViewModel::onHiveEditClick,
                onDeleteClick = {} // необходимо добавить удаление, сейчас мне лень
            )
            HiveContent(state.hive, actions)
        }
    }
}


@Composable
private fun HiveContent(hive: HiveUi, actions: HiveActions) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Title("Улей")

        MainInformation(hive)

        if (hive.connectedHub is HubUi.Present) {
            HubInformation(
                hive.connectedHub,
                actions.onTemperatureClick,
                actions.onNoiseClick,
                actions.onWeightClick
            )
        }

        if (hive.queen is QueenUi.Present) {
            QueenInformation(hive.queen, actions.onQueenClick)
        }

        NotificationsInformation(hive.notifications, actions.onNotificationClick)

        WorksInformation(hive.works, actions.onWorkClick)
    }
}

@Composable
private fun MainInformation(hive: HiveUi) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Основная информация",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Row {
            Text(text = "Название: ${hive.name}")

            if (hive.connectedHub is HubUi.Present) {
                Text(
                    text = "Подключенный хаб: ${hive.connectedHub.name}",
                    style =
                        MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            } else {
                Text(
                    text = "Подключенный хаб: нет",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
private fun HubInformation(
    hub: HubUi.Present,
    onTemperatureClick: () -> Unit,
    onNoiseClick: () -> Unit,
    onWeightClick: () -> Unit
) {
    // пока заглушка
}

@Composable
private fun QueenInformation(
    queen: QueenUi.Present,
    onQueenClick: () -> Unit
) {
    // пока заглушка
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Матка",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun NotificationsInformation(
    notifications: List<NotificationUi>,
    onNotificationClick: () -> Unit
) {
    // пока заглушка
}

@Composable
private fun WorksInformation(
    works: List<WorkUi>,
    onWorkClick: () -> Unit
) {
    // пока заглушка
}