package com.app.mobile.presentation.ui.screens.hive.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.presentation.models.hive.HiveUi
import com.app.mobile.presentation.models.hive.HubUi
import com.app.mobile.presentation.models.hive.NotificationUi
import com.app.mobile.presentation.models.hive.QueenUi
import com.app.mobile.presentation.models.hive.WorkUi
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.screens.hive.details.models.HiveActions
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveNavigationEvent
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveUiState
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveViewModel

@Composable
fun HiveScreen(
    hiveViewModel: HiveViewModel,
    onQueenClick: (queenId: String) -> Unit,
    onWorksClick: (hiveId: String) -> Unit,
    onNotificationsClick: (hiveId: String) -> Unit,
    onTemperatureClick: (hiveId: String) -> Unit,
    onNoiseClick: (hiveId: String) -> Unit,
    onWeightClick: (hiveId: String) -> Unit,
    onHiveListClick: () -> Unit,
    onHiveEditClick: (hiveId: String) -> Unit
) {
    val hiveUiState by hiveViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hiveViewModel.loadHive()
    }

    ObserveAsEvents(hiveViewModel.event) { event ->
        when (event) {
            is HiveNavigationEvent.NavigateToHiveList -> onHiveListClick()
            is HiveNavigationEvent.NavigateToQueenByHive -> onQueenClick(event.queenId)
            is HiveNavigationEvent.NavigateToWorkByHive -> onWorksClick(event.hiveId)
            is HiveNavigationEvent.NavigateToNotificationByHive -> onNotificationsClick(
                event.hiveId
            )

            is HiveNavigationEvent.NavigateToTemperatureByHive -> onTemperatureClick(event.hiveId)
            is HiveNavigationEvent.NavigateToNoiseByHive -> onNoiseClick(event.hiveId)
            is HiveNavigationEvent.NavigateToWeightByHive -> onWeightClick(event.hiveId)
            is HiveNavigationEvent.NavigateToHiveEdit -> onHiveEditClick(event.hiveId)
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
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
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
}

@Composable
private fun MainInformation(hive: HiveUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = hive.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val hubText = if (hive.connectedHub is HubUi.Present) {
                "Подключен к хабу: ${hive.connectedHub.name}"
            } else {
                "Нет подключения к хабу"
            }

            Text(
                text = hubText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Хаб: ${hub.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "IP: ${hub.ipAddress}:${hub.port}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SensorButton(
                    text = "Темп.",
                    icon = Icons.Default.Thermostat,
                    onClick = onTemperatureClick,
                    modifier = Modifier.weight(1f)
                )
                SensorButton(
                    text = "Шум",
                    icon = Icons.Default.GraphicEq,
                    onClick = onNoiseClick,
                    modifier = Modifier.weight(1f)
                )
                SensorButton(
                    text = "Вес",
                    icon = Icons.Default.MonitorWeight,
                    onClick = onWeightClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SensorButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun QueenInformation(
    queen: QueenUi.Present,
    onQueenClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onQueenClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Матка: ${queen.queen.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Details",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            val stage = queen.queen.stage
            Text(
                text = stage.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = stage.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            LinearProgressIndicator(
                progress = { stage.progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                trackColor = MaterialTheme.colorScheme.secondary
            )

            Text(
                text = stage.remainingDays,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun NotificationsInformation(
    notifications: List<NotificationUi>,
    onNotificationClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Уведомления",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onNotificationClick) {
                Text("Все")
            }
        }

        if (notifications.isEmpty()) {
            Text(
                text = "Нет новых уведомлений",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            notifications.take(3).forEach { notification ->
                NotificationItem(notification)
            }
        }
    }
}

@Composable
private fun NotificationItem(notification: NotificationUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = notification.dateTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WorksInformation(
    works: List<WorkUi>,
    onWorkClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Работы",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onWorkClick) {
                Text("Все")
            }
        }

        if (works.isEmpty()) {
            Text(
                text = "Нет запланированных работ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            works.take(3).forEach { work ->
                WorkItem(work)
            }
        }
    }
}

@Composable
private fun WorkItem(work: WorkUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = work.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = work.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = work.dateTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}