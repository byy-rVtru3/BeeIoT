package com.app.mobile.presentation.ui.screens.hive.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.hive.HivePreview
import com.app.mobile.presentation.ui.components.CustomFloatingActionButton
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.HiveItemCard
import com.app.mobile.presentation.ui.components.SelectorTopBar
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.screens.hive.list.models.HivesListActions
import com.app.mobile.presentation.ui.screens.hive.list.vewmodel.HivesListNavigationEvent
import com.app.mobile.presentation.ui.screens.hive.list.vewmodel.HivesListUiState
import com.app.mobile.presentation.ui.screens.hive.list.vewmodel.HivesListViewModel
import com.app.mobile.ui.theme.Dimens

@Composable
fun HivesListScreen(
    hivesListViewModel: HivesListViewModel,
    onHiveClick: (String) -> Unit,
    onCreateHiveClick: () -> Unit
) {
    val hivesListUiState by hivesListViewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab by hivesListViewModel.selectedTab.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hivesListViewModel.loadHives()
    }

    ObserveAsEvents(hivesListViewModel.event) { event ->
        when (event) {
            is HivesListNavigationEvent.NavigateToHive -> {
                onHiveClick(event.hiveId)
            }

            is HivesListNavigationEvent.NavigateToCreateHive -> {
                onCreateHiveClick()
            }
        }
    }

    when (val state = hivesListUiState) {
        is HivesListUiState.Loading -> FullScreenProgressIndicator()

        is HivesListUiState.Error -> ErrorMessage(
            message = state.message,
            onRetry = hivesListViewModel::onRetry
        )

        is HivesListUiState.Empty -> EmptyHivesListScreen(
            selectedTab = selectedTab,
            onTabSelected = hivesListViewModel::onTabSelected,
            onCreateHiveClick = hivesListViewModel::onCreateHiveClick
        )

        is HivesListUiState.Content -> {
            val actions = HivesListActions(
                onHiveClick = hivesListViewModel::onHiveClick,
                onCreateHiveClick = hivesListViewModel::onCreateHiveClick
            )
            HivesListContent(
                state.hives, actions,
                selectedTab = selectedTab,
                onTabSelected = hivesListViewModel::onTabSelected
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HivesListContent(
    hives: List<HivePreview>, actions: HivesListActions, selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(stringResource(R.string.active_hives), stringResource(R.string.archive))

    Scaffold(
        topBar = {
            SelectorTopBar(
                tabs = tabs,
                selectedTabIndex = selectedTab,
                onTabSelected = onTabSelected
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        floatingActionButton = {
            if (selectedTab == 0) {
                CustomFloatingActionButton(
                    onClick = actions.onCreateHiveClick,
                    icon = Icons.Filled.Add,
                    bottomPadding = Dimens.BottomAppBarHeight,
                    contentDescription = stringResource(R.string.add_hive)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> {
                    if (hives.isNotEmpty()) {

                        val fabSpace = Dimens.BottomAppBarHeight + Dimens.FabSize + Dimens.ItemsSpacingLarge

                        HivesList(
                            hives = hives,
                            actions = actions,
                            bottomPadding = fabSpace
                        )
                    } else {
                        EmptyStub(text = stringResource(R.string.empty_hives_list_screen))
                    }
                }
                1 -> {
                    EmptyStub(text = stringResource(R.string.empty_archive_hives_list_screen))
                }
            }
        }
    }
}

@Composable
private fun EmptyStub(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}
@Composable
private fun HivesList(
    hives: List<HivePreview>,
    actions: HivesListActions,
    bottomPadding: Dp,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenContentPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal),
        contentPadding = PaddingValues(
            top = Dimens.ScreenContentPadding,
            bottom = bottomPadding + Dimens.ScreenContentPadding
        )
    ) {
        items(hives) { hive ->
            HiveItem(hive, actions.onHiveClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HiveItem(hive: HivePreview, onHiveClick: (String) -> Unit) {
    HiveItemCard(
        name = hive.name,
        // TODO: Добавьте поле lastConnection в модель HivePreview
        lastConnection = "2024.04.12",

        // TODO: Добавьте поле isConnected (Boolean) в модель HivePreview
        isSignalActive = true, // Если true - иконка черная, false - серая

        onClick = { onHiveClick(hive.id) }
    )
}

@Composable
private fun EmptyHivesListScreen(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onCreateHiveClick: () -> Unit
) {
    val tabs = listOf(stringResource(R.string.active_hives), stringResource(R.string.archive))

    Scaffold(
        topBar = {
            SelectorTopBar(
                tabs = tabs,
                selectedTabIndex = selectedTab,
                onTabSelected = onTabSelected
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        floatingActionButton = {
            if (selectedTab == 0) {
                CustomFloatingActionButton(
                    onClick = onCreateHiveClick,
                    icon = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_hive),
                    bottomPadding = Dimens.BottomAppBarHeight
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            val emptyText = if (selectedTab == 0) {
                stringResource(R.string.empty_hives_list_screen)
            } else {
                stringResource(R.string.empty_archive_hives_list_screen)

            }
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}