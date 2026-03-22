package com.app.mobile.presentation.ui.screens.queen.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.queen.QueenPreviewListModel
import com.app.mobile.presentation.models.queen.QueenPreviewModel
import com.app.mobile.presentation.ui.components.EmptyStub
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.QueenCard
import com.app.mobile.presentation.ui.components.QueenCardDisplayMode
import com.app.mobile.presentation.ui.components.TabbedScreenScaffold
import com.app.mobile.presentation.ui.screens.queen.list.models.QueenListActions
import com.app.mobile.presentation.ui.screens.queen.list.viewmodel.QueenListEvent
import com.app.mobile.presentation.ui.screens.queen.list.viewmodel.QueenListUiState
import com.app.mobile.presentation.ui.screens.queen.list.viewmodel.QueenListViewModel
import com.app.mobile.ui.theme.Dimens

@Composable
fun QueenListScreen(
	queenListViewModel: QueenListViewModel,
	onQueenClick: (String) -> Unit,
	onAddClick: () -> Unit
) {
	val queenListUiState by queenListViewModel.uiState.collectAsStateWithLifecycle()
	val snackbarHostState = remember { SnackbarHostState() }

	// В HivesListScreen это в ViewModel, здесь пока сделаем локально для UI
	var selectedTab by remember { mutableIntStateOf(0) }

	LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
		queenListViewModel.loadQueens()
	}

	ObserveAsEvents(queenListViewModel.event) { event ->
		when (event) {
			is QueenListEvent.NavigateToQueen -> onQueenClick(event.queenName)
			is QueenListEvent.NavigateToAddQueen -> onAddClick()

			is QueenListEvent.ShowSnackBar -> {
				snackbarHostState.showSnackbar(
					event.message,
					duration = SnackbarDuration.Short
				)
			}
		}
	}

	when (val state = queenListUiState) {
		is QueenListUiState.Loading -> FullScreenProgressIndicator()

		is QueenListUiState.Error   -> ErrorMessage(state.message, onRetry = queenListViewModel::resetError)

		is QueenListUiState.Content -> {
			val actions = QueenListActions(
				onQueenClick = queenListViewModel::onQueenClick,
				onAddClick = queenListViewModel::onAddClick
			)

			// Фильтруем список, если бы логика была на UI (обычно это делается в VM)
			// Здесь просто передаем весь список для вкладки 0, для 1 - пусто
			val contentList = if (selectedTab == 0) state.queens else emptyList()

			QueenListContent(
				queens = contentList,
				snackbarHostState = snackbarHostState,
				actions = actions,
				selectedTab = selectedTab,
				onTabSelected = { index -> selectedTab = index }
			)
		}
	}
}

@Composable
fun QueenListContent(
	queens: List<QueenPreviewModel>,
	snackbarHostState: SnackbarHostState,
	actions: QueenListActions,
	selectedTab: Int,
	onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        stringResource(R.string.queens),
        stringResource(R.string.archive)
    )

    TabbedScreenScaffold(
        tabs = tabs,
        selectedTabIndex = selectedTab,
        onTabSelected = onTabSelected,
        showFabOnTab = 0,
        fabIcon = Icons.Filled.Add,
        fabContentDescription = stringResource(R.string.add_queen),
        onFabClick = actions.onAddClick
    ) { innerPadding ->
        when (selectedTab) {
            0 -> {
                if (queens.isNotEmpty()) {
                    QueensList(
                        queens = queens,
                        actions = actions,
                        modifier = innerPadding
                    )
                } else {
                    EmptyStub(
                        text = stringResource(R.string.empty_queens_list_screen),
                        modifier = innerPadding
                    )
                }
            }
            1 -> {
                // Заглушка для архива
                EmptyStub(
                    text = stringResource(R.string.empty_archive_list_screen),
                    modifier = innerPadding
                )
            }
        }
    }
}

@Composable
private fun QueensList(
	queens: List<QueenPreviewModel>,
	actions: QueenListActions,
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
		items(queens) { queen ->
			QueenItem(queen, actions.onQueenClick)
		}
	}
}

@Composable
private fun QueenItem(queen: QueenPreviewModel, onQueenClick: (String) -> Unit) {
	// ВАЖНО: Используем SHOW_HIVE, чтобы отобразить имя улья и день
	QueenCard(
		queen = queen,
		onClick = { onQueenClick(queen.name) },
		displayMode = QueenCardDisplayMode.SHOW_HIVE
	)
}
