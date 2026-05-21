package com.app.mobile.presentation.ui.screens.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.domain.models.notifications.NotificationRecord
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.modifiers.styleShadow
import com.app.mobile.presentation.ui.screens.notifications.viewmodel.NotificationsEvent
import com.app.mobile.presentation.ui.screens.notifications.viewmodel.NotificationsUiState
import com.app.mobile.presentation.ui.screens.notifications.viewmodel.NotificationsViewModel
import com.app.mobile.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DATE_FORMAT = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.event) { event ->
        when (event) {
            is NotificationsEvent.NavigateBack -> onBackClick()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        topBar = {
            NotificationsTopBar(
                hasItems = state is NotificationsUiState.Content,
                onBackClick = viewModel::onBackClick,
                onClearClick = viewModel::onClear
            )
        }
    ) { innerPadding ->
        when (val s = state) {
            is NotificationsUiState.Empty   -> NotificationsEmpty(innerPadding)
            is NotificationsUiState.Content -> NotificationsList(s.items, innerPadding)
        }
    }
}

@Composable
private fun NotificationsTopBar(
    hasItems: Boolean,
    onBackClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .styleShadow()
            .clip(
                RoundedCornerShape(
                    bottomStart = Dimens.BorderRadiusMedium,
                    bottomEnd = Dimens.BorderRadiusMedium
                )
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = Dimens.TopBarHorizontalPadding)
                .size(Dimens.TopBarHeight)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = stringResource(R.string.notification_history),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (hasItems) {
                TextButton(
                    onClick = onClearClick,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.notification_clear),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(Dimens.IconSizeMedium)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationsEmpty(innerPadding: PaddingValues) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(Dimens.ScreenContentPadding)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingMedium)
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                modifier = Modifier.size(Dimens.Size72),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Text(
                text = stringResource(R.string.notification_history_empty),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.notification_history_empty_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NotificationsList(items: List<NotificationRecord>, innerPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentPadding = PaddingValues(
            horizontal = Dimens.ScreenContentPadding,
            vertical = Dimens.ScreenContentPadding
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingMedium)
    ) {
        items(items, key = { it.id }) { record ->
            NotificationCard(record)
        }
    }
}

@Composable
private fun NotificationCard(record: NotificationRecord) {
    val isCritical = record.type == "CRITICAL"
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.ItemCardPadding),
            horizontalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingMedium),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (isCritical) Icons.Outlined.NotificationsActive else Icons.Outlined.Notifications,
                contentDescription = null,
                modifier = Modifier.size(Dimens.IconSizeMedium),
                tint = if (isCritical) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingSmall)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (isCritical) stringResource(R.string.notification_type_critical)
                        else stringResource(R.string.notification_type_regular),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCritical) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                }
                if (record.body.isNotBlank()) {
                    Text(
                        text = record.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                Text(
                    text = DATE_FORMAT.format(Date(record.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }
        }
    }
}
