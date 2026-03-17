package com.app.mobile.presentation.ui.screens.hive.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.app.mobile.presentation.ui.components.SelectorTopBar
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

		is HivesListUiState.Empty   -> EmptyHivesListScreen(
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
				state.hives,
				snackbarHostState,
				actions,
				selectedTab = selectedTab,
				onTabSelected = hivesListViewModel::onTabSelected
			)
		}
	}

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HivesListContent(
	hives: List<HivePreview>,
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
        when (selectedTab) {
            0 -> {
                if (hives.isNotEmpty()) {
                    HivesList(
                        hives = hives,
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
                EmptyStub(
                    text = stringResource(R.string.empty_archive_list_screen),
                    modifier = innerPadding
                )
            }
        }
    }
}

@Composable
private fun HivesList(
	hives: List<HivePreview>,
	actions: HivesListActions,
	modifier: Modifier = Modifier
) {
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

		onClick = { onHiveClick(hive.name) }
	)
}

@Composable
private fun EmptyHivesListScreen(
	selectedTab: Int,
	onTabSelected: (Int) -> Unit,
	onCreateHiveClick: () -> Unit
) {
    val tabs = listOf(stringResource(R.string.active_hives), stringResource(R.string.archive))

    val emptyText = if (selectedTab == 0) {
        stringResource(R.string.empty_hives_list_screen)
    } else {
        stringResource(R.string.empty_archive_list_screen)
    }

    TabbedScreenScaffold(
        tabs = tabs,
        selectedTabIndex = selectedTab,
        onTabSelected = onTabSelected,
        showFabOnTab = 0,
        fabIcon = Icons.Filled.Add,
        fabContentDescription = stringResource(R.string.add_hive),
        onFabClick = onCreateHiveClick
    ) { padding ->
        EmptyStub(
            text = emptyText,
            modifier = padding
        )
    }
}