package com.app.mobile.presentation.ui.screens.works.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.hive.WorkUi
import com.app.mobile.presentation.ui.components.AppTopBar
import com.app.mobile.presentation.ui.components.CustomFloatingActionButton
import com.app.mobile.presentation.ui.components.EmptyStub
import com.app.mobile.presentation.ui.components.WorkTileCard
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import com.app.mobile.presentation.ui.screens.works.list.viewmodel.WorksListEvent
import com.app.mobile.presentation.ui.screens.works.list.viewmodel.WorksListUiState
import com.app.mobile.presentation.ui.screens.works.list.viewmodel.WorksListViewModel
import com.app.mobile.ui.theme.Dimens

@Composable
fun WorksListScreen(
	worksListViewModel: WorksListViewModel,
	onWorkClick: (workId: String, hiveId: String) -> Unit,
	onCreateClick: (hiveId: String) -> Unit,
	onBackClick: () -> Unit
) {
	val worksUiState by worksListViewModel.uiState.collectAsStateWithLifecycle()
	val snackBarHostState = remember { SnackbarHostState() }

	LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
		worksListViewModel.loadWorks()
	}

	ObserveAsEvents(worksListViewModel.event) { event ->
		when (event) {
			is WorksListEvent.NavigateToWorkDetail -> onWorkClick(event.workId, event.hiveId)
			is WorksListEvent.NavigateToWorkCreate -> onCreateClick(event.hiveId)
			is WorksListEvent.NavigateBack         -> onBackClick()

			is WorksListEvent.ShowSnackBar         -> {
				snackBarHostState.showSnackbar(
					message = event.message,
					duration = SnackbarDuration.Short
				)
			}
		}
	}

	when (val state = worksUiState) {
		is WorksListUiState.Loading -> FullScreenProgressIndicator()
		is WorksListUiState.Error   -> ErrorMessage(state.message, worksListViewModel::resetError)
		is WorksListUiState.Content -> WorksListContent(
			state.works,
			worksListViewModel::onWorkClick,
			snackBarHostState = snackBarHostState,
			worksListViewModel::onCreateClick,
			onNavigateBack = onBackClick
		)
	}
}

@Composable
fun WorksListContent(
	works: List<WorkUi>,
	onWorkClick: (String) -> Unit,
	snackBarHostState: SnackbarHostState,
	onCreateClick: () -> Unit,
	onNavigateBack: () -> Unit
) {
	Scaffold(
		topBar = {
			AppTopBar(
				title = stringResource(R.string.works_for_hive),
				onBackClick = onNavigateBack
			)
		},
		snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
		containerColor = MaterialTheme.colorScheme.surfaceVariant,
		contentWindowInsets = WindowInsets.safeDrawing,
		floatingActionButton = {
			CustomFloatingActionButton(
				onClick = onCreateClick,
				icon = Icons.Filled.Add,
				contentDescription = stringResource(R.string.add)
			)
		}
	) { innerPadding ->
		if (works.isNotEmpty()) {
			WorksList(
				works = works,
				onWorkClick = onWorkClick,
				modifier = Modifier.padding(innerPadding)
			)
		} else {
			EmptyStub(
				text = stringResource(R.string.empty_works_list),
				modifier = Modifier.padding(innerPadding)
			)
		}
	}
}

@Composable
private fun WorksList(
	works: List<WorkUi>,
	onWorkClick: (String) -> Unit,
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
		items(
			items = works,
			key = { it.id }
		) { work ->
			WorkTileCard(
				title = work.title,
				dateTime = work.dateTime,
				onClick = { onWorkClick(work.id) }
			)
		}
	}
}
