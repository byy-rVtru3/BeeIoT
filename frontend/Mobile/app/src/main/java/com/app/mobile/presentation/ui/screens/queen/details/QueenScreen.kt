package com.app.mobile.presentation.ui.screens.queen.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.queen.QueenUiModel
import com.app.mobile.presentation.models.queen.StageType
import com.app.mobile.presentation.models.queen.TimelineItem
import com.app.mobile.presentation.ui.components.*
import com.app.mobile.presentation.ui.screens.queen.details.viewmodel.QueenNavigationEvent
import com.app.mobile.presentation.ui.screens.queen.details.viewmodel.QueenUiState
import com.app.mobile.presentation.ui.screens.queen.details.viewmodel.QueenViewModel
import com.app.mobile.ui.theme.Dimens

@Composable
fun QueenScreen(
    queenViewModel: QueenViewModel,
    onEditClick: (queenId: String) -> Unit,
    onHiveClick: (hiveId: String) -> Unit,
    onBackClick: () -> Unit,
    onDeleteClick: (queenId: String) -> Unit // Добавили колбэк удаления
) {
    val queenUiState by queenViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        queenViewModel.getQueen()
    }

    ObserveAsEvents(queenViewModel.event) { event ->
        when (event) {
            is QueenNavigationEvent.NavigateToEditQueen -> onEditClick(event.queenId)
            is QueenNavigationEvent.NavigateToHive -> onHiveClick(event.hiveId)
        }
    }

    when (val state = queenUiState) {
        is QueenUiState.Loading -> FullScreenProgressIndicator()
        is QueenUiState.Error -> ErrorMessage(state.message, onRetry = {})
        is QueenUiState.Content -> {
            QueenContent(
                queen = state.queen,
                onEditClick = { queenViewModel.onEditQueenClick() },
                onHiveClick = { queenViewModel.onHiveClick() },
                onDeleteClick = { onDeleteClick(state.queen.id) },
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
private fun QueenContent(
    queen: QueenUiModel,
    onEditClick: () -> Unit,
    onHiveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.queen),
                onBackClick = onBackClick,
                // Меняем Edit на Delete
                action = TopBarAction.Delete(onClick = onDeleteClick)
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Dimens.ScreenContentPadding,
                        end = Dimens.ScreenContentPadding,
                        top = Dimens.ScreenContentPadding,
                        bottom = Dimens.ItemSpacingNormal
                    ),
                verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingLarge)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        InfoCard(
                            title = stringResource(R.string.label_name),
                            value = queen.name,
                            modifier = Modifier.weight(1f)
                        )

                        val hiveName = queen.hive?.name ?: stringResource(R.string.no_hive)
                        val hiveModifier = if (queen.hive != null) {
                            Modifier
                                .weight(1f)
                                .clickable { onHiveClick() }
                        } else {
                            Modifier.weight(1f)
                        }

                        InfoCard(
                            title = stringResource(R.string.hive_format),
                            value = hiveName,
                            modifier = hiveModifier
                        )
                    }

                    // 2. Индикатор прогресса
                    QueenStatusSection(queen)
                }
                // 3. Заголовок раздела
                SectionTitle(title = "График развития")
            }

            // --- СКРОЛЛЯЩИЙСЯ СПИСОК (Timeline) ---
            if (queen.timeline.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = Dimens.ScreenContentPadding,
                        end = Dimens.ScreenContentPadding,
                        bottom = Dimens.ScreenContentPadding + Dimens.FabSize + Dimens.ItemsSpacingLarge // Отступ под FAB
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
                ) {
                    items(queen.timeline) { item ->
                        TimelineItemView(item)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Нет данных о развитии",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QueenStatusSection(queen: QueenUiModel) {
    val currentStage = queen.timeline.find { it.isToday }
        ?: queen.timeline.lastOrNull { it.isCompleted }
        ?: queen.timeline.firstOrNull()

    val totalStages = queen.timeline.size
    val completedStages = queen.timeline.count { it.isCompleted }
    val progress = if (totalStages > 0) completedStages.toFloat() / totalStages.toFloat() else 0f

    val statusColor = MaterialTheme.colorScheme.primary

    Surface(
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.ItemCardPadding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemCardTextPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentStage?.title ?: "Старт",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currentStage?.dateFormatted ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.BorderWidthThick),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (progress >= 1f) "Завершено" else "В процессе",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = currentStage?.description ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TimelineItemView(item: TimelineItem) {
    val isToday = item.isToday
    val isCompleted = item.isCompleted

    val containerColor =
        if (isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor =
        if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    val iconTint = if (isToday) MaterialTheme.colorScheme.onPrimary
    else if (isCompleted) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    val iconBg = if (isToday) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        color = containerColor,
        shadowElevation = if (isToday) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(Dimens.ItemCardPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getStageIcon(item.stageType),
                    contentDescription = null,
                    tint = iconTint
                )
            }

            Spacer(modifier = Modifier.width(Dimens.ItemCardTextPadding))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
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
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor
                    )
                }
            }

            if (isCompleted && !isToday) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
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