package com.app.mobile.presentation.ui.screens.main

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.presentation.ui.screens.settings.NotificationBottomSheet
import com.app.mobile.R
import com.app.mobile.domain.models.hives.HiveDomainPreview
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.domain.models.hives.WorkDomain
import com.app.mobile.domain.models.hives.queen.QueenDomainPreview
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.modifiers.styleShadow
import com.app.mobile.presentation.ui.screens.main.viewmodel.HomeEvent
import com.app.mobile.presentation.ui.screens.main.viewmodel.HomeUiState
import com.app.mobile.presentation.ui.screens.main.viewmodel.HomeViewModel
import com.app.mobile.ui.theme.Dimens
import java.time.format.DateTimeFormatter

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd")
private const val WORK_ITEM_MAX_LINES = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onHiveClick: (String) -> Unit,
    onQueenClick: (String) -> Unit,
    onHubClick: (String) -> Unit,
    onWorkClick: (String, String) -> Unit
) {
    val state by homeViewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing = (state as? HomeUiState.Content)?.isRefreshing ?: false

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            homeViewModel.onAcceptNotificationPrompt()
        } else {
            homeViewModel.onDeclineNotificationPrompt()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        homeViewModel.loadData()
    }

    ObserveAsEvents(homeViewModel.event) { event ->
        when (event) {
            is HomeEvent.NavigateToHive  -> onHiveClick(event.hiveName)
            is HomeEvent.NavigateToQueen -> onQueenClick(event.queenName)
            is HomeEvent.NavigateToHub   -> onHubClick(event.hubId)
            is HomeEvent.NavigateToWork  -> onWorkClick(event.workId, event.hiveId)
        }
    }

    if ((state as? HomeUiState.Content)?.showNotificationPrompt == true) {
        NotificationBottomSheet(
            onEnableClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    homeViewModel.onAcceptNotificationPrompt()
                }
            },
            onDeclineClick = homeViewModel::onDeclineNotificationPrompt
        )
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = homeViewModel::refresh
    ) {
        when (val s = state) {
            is HomeUiState.Loading -> FullScreenProgressIndicator()
            is HomeUiState.Error   -> ErrorMessage(message = s.message, onRetry = homeViewModel::onRetry)
            is HomeUiState.Content -> HomeContent(content = s, homeViewModel = homeViewModel)
        }
    }
}

@Composable
private fun HomeContent(
    content: HomeUiState.Content,
    homeViewModel: HomeViewModel
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,

        topBar = { HomeTopBar() }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = Dimens.ScreenContentPadding,
                vertical = Dimens.ScreenContentPadding
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingLarge)
        ) {
            item {
                SectionTitle(text = stringResource(R.string.home_section_hives))
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingLarge)) {
                    items(content.hives) { hive ->
                        HiveCard(hive = hive, onClick = { homeViewModel.onHiveClick(hive.name) })
                    }
                }
            }

            item {
                SectionTitle(text = stringResource(R.string.home_section_queens))
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingLarge)) {
                    items(content.queens) { queen ->
                        QueenCard(queen = queen, onClick = { homeViewModel.onQueenClick(queen.name) })
                    }
                }
            }

            item {
                SectionTitle(text = stringResource(R.string.home_section_hubs))
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingLarge)) {
                    items(content.hubs) { hub ->
                        HubCard(hub = hub, onClick = { homeViewModel.onHubClick(hub.id) })
                    }
                }
            }

            item {
                SectionTitle(text = stringResource(R.string.home_section_works))
            }
            items(content.works) { work ->
                WorkItem(
                    work = work,
                    onClick = { homeViewModel.onWorkClick(work.id, work.hiveId) }
                )
            }
        }
    }
}

@Composable
private fun HomeTopBar() {
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
                .height(Dimens.TopBarHeight)
        ) {
            Text(
                text = stringResource(R.string.home),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = Dimens.SectionTitleLeftPadding)
    )
}

@Composable
private fun HiveCard(hive: HiveDomainPreview, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        modifier = Modifier
            .width(Dimens.HiveCardWidth)
            .height(Dimens.HiveCardHeight)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.ItemCardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = hive.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_hives),
                contentDescription = null,
                modifier = Modifier.size(Dimens.HiveItemCardIconSize),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun QueenCard(queen: QueenDomainPreview, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        modifier = Modifier
            .width(Dimens.HiveCardWidth)
            .height(Dimens.HiveCardHeight)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.ItemCardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingSmall)
            ) {
                Text(
                    text = queen.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.birth_date_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = queen.startDate.format(DATE_FORMATTER),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_queens),
                contentDescription = null,
                modifier = Modifier.size(Dimens.HiveItemCardIconSize),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun HubCard(hub: HubDomain, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        modifier = Modifier
            .width(Dimens.HubCardWidth)
            .height(Dimens.HubCardHeight)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimens.ItemCardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = hub.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_sensors),
                contentDescription = null,
                modifier = Modifier.size(Dimens.HiveItemCardIconSize),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun WorkItem(work: WorkDomain, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(Dimens.ItemCardRadius),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.ItemCardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingSmall)) {
                Text(
                    text = work.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = work.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = WORK_ITEM_MAX_LINES,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = work.dateTime.format(DATE_FORMATTER),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
    }
}
