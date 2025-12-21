package com.app.mobile.presentation.ui.screens.hive.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.app.mobile.presentation.models.hive.HiveUi
import com.app.mobile.presentation.models.hive.HubUi
import com.app.mobile.presentation.models.hive.NotificationUi
import com.app.mobile.presentation.models.hive.QueenUi
import com.app.mobile.presentation.models.hive.WorkUi
import com.app.mobile.presentation.ui.components.AppTopBar
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.TopBarAction
import com.app.mobile.presentation.ui.screens.hive.details.models.HiveActions
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveNavigationEvent
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveUiState
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveViewModel
import com.app.mobile.ui.theme.Dimens
import kotlinx.coroutines.flow.collectLatest
import com.app.mobile.R

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
    val hiveUiState by hiveViewModel.hiveUiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hiveViewModel.loadHive()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(hiveViewModel.navigationEvent) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            hiveViewModel.navigationEvent.collectLatest { event ->
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
            HiveContent(state.hive, actions, onBackClick = onHiveListClick)
        }
    }
}

@Composable
private fun HiveContent(
    hive: HiveUi,
    actions: HiveActions,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.hive_title_details),
                onBackClick = onBackClick,
                action = TopBarAction.Delete(onClick = actions.onDeleteClick)
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant // Серый фон
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingLarge)
        ) {
            // 1. Основная информация
            SectionTitle(title = "Основная информация")
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal),
                modifier = Modifier.fillMaxWidth()
            ) {
                InfoCard(
                    title = "Название:",
                    value = hive.name,
                    modifier = Modifier.weight(1f)
                )

                val hubName = if (hive.connectedHub is HubUi.Present) {
                    hive.connectedHub.name
                } else {
                    "Нет"
                }
                InfoCard(
                    title = "Подключенный хаб:",
                    value = hubName,
                    modifier = Modifier.weight(1f)
                )
            }

            // 2. Хаб и сенсоры
            if (hive.connectedHub is HubUi.Present) {
                SectionTitle(title = "Данные хаба")
                HubControlCard(
                    hub = hive.connectedHub,
                    onTempClick = actions.onTemperatureClick,
                    onNoiseClick = actions.onNoiseClick,
                    onWeightClick = actions.onWeightClick
                )
            }

            // 3. Матка
            if (hive.queen is QueenUi.Present) {
                SectionTitle(title = "Матка")
                // Используем actions.onQueenClick без аргументов, так как ID прошит выше
                QueenCard(queen = hive.queen, onClick = actions.onQueenClick)
            }

            // 4. Уведомления
            if (hive.notifications.isNotEmpty()) {
                SectionHeaderWithAction(
                    title = "Уведомления",
                    actionText = "Посмотреть все",
                    onActionClick = actions.onNotificationClick
                )
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)) {
                    hive.notifications.take(2).forEach { notification ->
                        NotificationItemCard(notification)
                    }
                }
            }

            // 5. Работы
            if (hive.works.isNotEmpty()) {
                SectionHeaderWithAction(
                    title = "Работы по улью",
                    actionText = "Посмотреть все",
                    onActionClick = actions.onWorkClick
                )
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)) {
                    hive.works.take(2).forEach { work ->
                        WorkItemCard(work)
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.ItemsSpacingLarge))
        }
    }
}

// --- UI Components ---

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun SectionHeaderWithAction(
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = actionText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(Dimens.ItemCardPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HubControlCard(
    hub: HubUi.Present,
    onTempClick: () -> Unit,
    onNoiseClick: () -> Unit,
    onWeightClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Dimens.ItemCardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Информация о хабе
            Column {
                Text(
                    text = "Хаб: ${hub.name}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "IP: ${hub.ipAddress}:${hub.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Горизонтальная линия
            // Обратите внимание: Divider устарел в новых версиях M3, лучше использовать HorizontalDivider
            // Но если у вас старая версия, оставьте Divider
            androidx.compose.material3.HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            // Кнопки сенсоров (ТЕКСТОВЫЕ)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Расстояние между кнопками
            ) {
                // Weight(1f) растянет кнопки равномерно по ширине
                SensorActionItem(
                    label = "Темп.",
                    onClick = onTempClick,
                    modifier = Modifier.weight(1f)
                )
                SensorActionItem(
                    label = "Шум",
                    onClick = onNoiseClick,
                    modifier = Modifier.weight(1f)
                )
                SensorActionItem(
                    label = "Вес",
                    onClick = onWeightClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SensorActionItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Используем Surface или OutlinedButton, чтобы текст выглядел как кнопка
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp), // Скругление углов кнопки
        color = MaterialTheme.colorScheme.secondaryContainer, // Цвет фона кнопки
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 12.dp) // Отступы внутри кнопки
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge, // Шрифт чуть крупнее для читаемости
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun QueenCard(queen: QueenUi.Present, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(Dimens.ItemCardPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = queen.queen.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = queen.queen.stage.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NotificationItemCard(notification: NotificationUi) {
    Surface(
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.ItemCardPadding)
                .fillMaxWidth()
        ) {
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notification.dateTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun WorkItemCard(work: WorkUi) {
    Surface(
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.ItemCardPadding)
                .fillMaxWidth()
        ) {
            Text(
                text = work.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = work.text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = work.dateTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}