package com.app.mobile.presentation.ui.screens.hub.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.hive.HubPreviewModel
import com.app.mobile.presentation.ui.components.EmptyStub
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.HubTileCard
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.TabbedScreenScaffold
import com.app.mobile.presentation.ui.screens.hub.list.models.HubsListActions
import com.app.mobile.presentation.ui.screens.hub.list.viewmodel.HubsListEvent
import com.app.mobile.presentation.ui.screens.hub.list.viewmodel.HubsListUiState
import com.app.mobile.presentation.ui.screens.hub.list.viewmodel.HubsListViewModel
import com.app.mobile.ui.theme.Dimens

@Composable
fun HubsListScreen(
    hubsListViewModel: HubsListViewModel,
    onHubClick: (String) -> Unit,
    onCreateHubClick: () -> Unit
) {
    val hubsListUiState by hubsListViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val selectedTab by hubsListViewModel.selectedTab.collectAsStateWithLifecycle()
    val isRefreshing by hubsListViewModel.isRefreshing.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hubsListViewModel.loadHubs()
    }

    ObserveAsEvents(hubsListViewModel.event) { event ->
        when (event) {
            is HubsListEvent.NavigateToHub -> onHubClick(event.hubId)
            is HubsListEvent.NavigateToCreateHub -> onCreateHubClick()
            is HubsListEvent.ShowSnackBar -> snackbarHostState.showSnackbar(
                event.message,
                duration = SnackbarDuration.Short
            )
        }
    }

    when (val state = hubsListUiState) {
        is HubsListUiState.Loading -> FullScreenProgressIndicator()

        is HubsListUiState.Error -> ErrorMessage(
            message = state.message,
            onRetry = hubsListViewModel::onRetry
        )

        is HubsListUiState.Empty -> EmptyHubsListScreen(
            selectedTab = selectedTab,
            onTabSelected = hubsListViewModel::onTabSelected,
            onCreateHubClick = hubsListViewModel::onCreateHubClick,
            isRefreshing = isRefreshing,
            onRefresh = hubsListViewModel::refreshHubs
        )

        is HubsListUiState.Content -> {
            val actions = HubsListActions(
                onHubClick = hubsListViewModel::onHubClick,
                onCreateHubClick = hubsListViewModel::onCreateHubClick
            )
            HubsListContent(
                hubs = state.hubs,
                snackbarHostState = snackbarHostState,
                actions = actions,
                selectedTab = selectedTab,
                onTabSelected = hubsListViewModel::onTabSelected,
                isRefreshing = isRefreshing,
                onRefresh = hubsListViewModel::refreshHubs
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HubsListContent(
    hubs: List<HubPreviewModel>,
    snackbarHostState: SnackbarHostState,
    actions: HubsListActions,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val tabs = listOf(stringResource(R.string.my_hubs), stringResource(R.string.archive))

    TabbedScreenScaffold(
        tabs = tabs,
        selectedTabIndex = selectedTab,
        onTabSelected = onTabSelected,
        showFabOnTab = 0,
        fabIcon = Icons.Filled.Add,
        fabContentDescription = stringResource(R.string.add_hub),
        onFabClick = actions.onCreateHubClick
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = innerPadding
        ) {
            when (selectedTab) {
                0 -> {
                    if (hubs.isNotEmpty()) {
                        HubsGrid(
                            hubs = hubs,
                            actions = actions
                        )
                    } else {
                        EmptyStub(
                            text = stringResource(R.string.empty_hubs_list_screen)
                        )
                    }
                }
                1 -> EmptyStub(
                    text = stringResource(R.string.empty_archive_list_screen)
                )
            }
        }
    }
}

@Composable
private fun HubsGrid(
    hubs: List<HubPreviewModel>,
    actions: HubsListActions,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenContentPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal),
        horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal),
        contentPadding = PaddingValues(
            top = Dimens.ScreenContentPadding,
            bottom = Dimens.ScreenContentPadding
        )
    ) {
        items(hubs) { hub ->
            HubTileCard(
                name = hub.name,
                // TODO: Добавить поле isConnected в HubPreviewModel
                isSignalActive = true,
                onClick = { actions.onHubClick(hub.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmptyHubsListScreen(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onCreateHubClick: () -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val tabs = listOf(stringResource(R.string.my_hubs), stringResource(R.string.archive))

    val emptyText = if (selectedTab == 0) {
        stringResource(R.string.empty_hubs_list_screen)
    } else {
        stringResource(R.string.empty_archive_list_screen)
    }

    TabbedScreenScaffold(
        tabs = tabs,
        selectedTabIndex = selectedTab,
        onTabSelected = onTabSelected,
        showFabOnTab = 0,
        fabIcon = Icons.Filled.Add,
        fabContentDescription = stringResource(R.string.add_hub),
        onFabClick = onCreateHubClick
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = padding
        ) {
            EmptyStub(text = emptyText)
        }
    }
}
