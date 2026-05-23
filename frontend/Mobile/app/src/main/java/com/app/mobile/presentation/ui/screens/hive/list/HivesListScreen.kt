package com.app.mobile.presentation.ui.screens.hive.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.hive.HivePreview
import com.app.mobile.presentation.ui.components.EmptyStub
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.HiveItemCard
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.SwipeToDeleteContainer
import com.app.mobile.presentation.ui.components.TabbedScreenScaffold
import com.app.mobile.presentation.ui.screens.hive.list.models.HivesListActions
import com.app.mobile.presentation.ui.screens.hive.list.vewmodel.HivesListEvent
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

	val snackbarHostState = remember { SnackbarHostState() }

	val selectedTab by hivesListViewModel.selectedTab.collectAsStateWithLifecycle()
	LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
		hivesListViewModel.loadHives()
	}

	ObserveAsEvents(hivesListViewModel.event) { event ->
		when (event) {
			is HivesListEvent.NavigateToHive -> {
				onHiveClick(event.hiveName)
			}

			is HivesListEvent.NavigateToCreateHive -> {
				onCreateHiveClick()
			}

			is HivesListEvent.ShowSnackBar -> {
				snackbarHostState.showSnackbar(
					event.message,
					duration = SnackbarDuration.Short
				)
			}
		}
	}

	when (val state = hivesListUiState) {
		is HivesListUiState.Loading -> FullScreenProgressIndicator()

		is HivesListUiState.Error   -> ErrorMessage(
			message = state.message,
			onRetry = hivesListViewModel::onRetry
		)

		is HivesListUiState.Content -> {
			val actions = HivesListActions(
				onHiveClick = hivesListViewModel::onHiveClick,
				onCreateHiveClick = hivesListViewModel::onCreateHiveClick,
				onDeleteHive = hivesListViewModel::onDeleteHive,
				onArchiveHive = hivesListViewModel::onArchiveHive,
				onUnarchiveHive = hivesListViewModel::onUnarchiveHive
			)
			HivesListContent(
				activeHives = state.activeHives,
				archivedHives = state.archivedHives,
				isRefreshing = state.isRefreshing,
				onRefresh = hivesListViewModel::refresh,
				snackbarHostState = snackbarHostState,
				actions = actions,
				selectedTab = selectedTab,
				onTabSelected = hivesListViewModel::onTabSelected
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HivesListContent(
	activeHives: List<HivePreview>,
	archivedHives: List<HivePreview>,
	isRefreshing: Boolean,
	onRefresh: () -> Unit,
	snackbarHostState: SnackbarHostState,
	actions: HivesListActions,
	selectedTab: Int,
	onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(stringResource(R.string.active_hives), stringResource(R.string.archive))

    TabbedScreenScaffold(
        tabs = tabs,
        selectedTabIndex = selectedTab,
        onTabSelected = onTabSelected,
        showFabOnTab = 0,
        fabIcon = Icons.Filled.Add,
        fabContentDescription = stringResource(R.string.add_hive),
        onFabClick = actions.onCreateHiveClick
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> {
                    if (activeHives.isNotEmpty()) {
                        ActiveHivesList(
                            hives = activeHives,
                            actions = actions,
                            modifier = innerPadding
                        )
                    } else {
                        EmptyStub(
                            text = stringResource(R.string.empty_hives_list_screen),
                            modifier = innerPadding
                        )
                    }
                }
                1 -> {
                    if (archivedHives.isNotEmpty()) {
                        ArchivedHivesList(
                            hives = archivedHives,
                            actions = actions,
                            modifier = innerPadding
                        )
                    } else {
                        EmptyStub(
                            text = stringResource(R.string.empty_archive_list_screen),
                            modifier = innerPadding
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveHivesList(
	hives: List<HivePreview>,
	actions: HivesListActions,
	modifier: Modifier = Modifier
) {
	val archiveIcon = ImageVector.vectorResource(R.drawable.ic_archive)
	val trashIcon = ImageVector.vectorResource(R.drawable.ic_trash)

	LazyColumn(
		modifier = modifier
			.fillMaxSize()
			.padding(horizontal = Dimens.ScreenContentPadding),
		verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal),
		contentPadding = PaddingValues(
			top = Dimens.ScreenContentPadding,
			bottom = Dimens.ScreenContentPadding
		)
	) {
		items(hives, key = { it.name }) { hive ->
			SwipeToDeleteContainer(
				onSwipeToEnd = { actions.onArchiveHive(hive.name) },
				onSwipeToStart = { actions.onDeleteHive(hive.name) },
				endIcon = archiveIcon,
				startIcon = trashIcon,
				endColor = Color(0xFFE65100),
				modifier = Modifier.animateItem()
			) {
				HiveItem(hive, actions.onHiveClick)
			}
		}
	}
}

@Composable
private fun ArchivedHivesList(
	hives: List<HivePreview>,
	actions: HivesListActions,
	modifier: Modifier = Modifier
) {
	val returnIcon = ImageVector.vectorResource(R.drawable.ic_return)

	LazyColumn(
		modifier = modifier
			.fillMaxSize()
			.padding(horizontal = Dimens.ScreenContentPadding),
		verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal),
		contentPadding = PaddingValues(
			top = Dimens.ScreenContentPadding,
			bottom = Dimens.ScreenContentPadding
		)
	) {
		items(hives, key = { it.name }) { hive ->
			SwipeToDeleteContainer(
				onSwipeToEnd = { actions.onUnarchiveHive(hive.name) },
				endIcon = returnIcon,
				endColor = Color(0xFF2E7D32),
				modifier = Modifier.animateItem()
			) {
				HiveItem(hive, actions.onHiveClick)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HiveItem(hive: HivePreview, onHiveClick: (String) -> Unit) {
	HiveItemCard(
		name = hive.name,
		lastConnection = "2024.04.12",
		isSignalActive = true,
		onClick = { onHiveClick(hive.name) }
	)
}
