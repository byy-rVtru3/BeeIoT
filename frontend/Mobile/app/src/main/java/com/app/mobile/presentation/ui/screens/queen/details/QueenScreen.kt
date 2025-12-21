package com.app.mobile.presentation.ui.screens.queen.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.presentation.models.queen.QueenUiModel
import com.app.mobile.presentation.models.queen.StageType
import com.app.mobile.presentation.models.queen.TimelineItem
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.screens.queen.details.viewmodel.QueenNavigationEvent
import com.app.mobile.presentation.ui.screens.queen.details.viewmodel.QueenUiState
import com.app.mobile.presentation.ui.screens.queen.details.viewmodel.QueenViewModel

@Composable
fun QueenScreen(
    queenViewModel: QueenViewModel,
    onEditClick: (queenId: String) -> Unit,
    onHiveClick: (hiveId: String) -> Unit
) {
    val queenUiState by queenViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        queenViewModel.getQueen()
    }

    ObserveAsEvents(queenViewModel.event) { event ->
        when (event) {
            is QueenNavigationEvent.NavigateToEditQueen -> {
                onEditClick(event.queenId)
            }

            is QueenNavigationEvent.NavigateToHive -> {
                onHiveClick(event.hiveId)
            }
        }
    }

    when (val state = queenUiState) {
        is QueenUiState.Loading -> {
            FullScreenProgressIndicator()
        }

        is QueenUiState.Error ->
            ErrorMessage(state.message, onRetry = {})

        is QueenUiState.Content -> {
            QueenContent(
                queen = state.queen,
                onEditClick = queenViewModel::onEditQueenClick,
                onHiveClick = queenViewModel::onHiveClick
            )
        }
    }
}

@Composable
private fun QueenContent(
    queen: QueenUiModel,
    onEditClick: () -> Unit,
    onHiveClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = queen.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        IconButton(onClick = onEditClick) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Редактировать",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (queen.hive != null) {
                        Text(
                            text = "Улей: ${queen.hive.name}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.clickable { onHiveClick() }
                        )
                    } else {
                        Text(
                            text = "Улей не назначен",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Text(
                text = "График развития",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            // Timeline
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(queen.timeline) { item ->
                    TimelineItemView(item)
                }
            }
        }
    }
}

@Composable
private fun TimelineItemView(item: TimelineItem) {
    val containerColor = if (item.isToday) {
        MaterialTheme.colorScheme.primaryContainer
    } else if (item.isCompleted) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (item.isToday) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else if (item.isCompleted) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isToday) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getStageIcon(item.stageType),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (item.isToday) FontWeight.Bold else FontWeight.Normal,
                        color = contentColor
                    )
                    Text(
                        text = item.dateFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor
                    )
                }

                if (item.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                }
            }

            if (item.isCompleted) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun getStageIcon(stageType: StageType): ImageVector {
    return when (stageType) {
        StageType.EGG -> Icons.Default.Circle
        StageType.LARVA -> Icons.Default.BugReport
        StageType.PUPA -> Icons.Default.HourglassEmpty
        StageType.QUEEN -> Icons.Default.Star
        StageType.ATTENTION -> Icons.Default.Warning
    }
}