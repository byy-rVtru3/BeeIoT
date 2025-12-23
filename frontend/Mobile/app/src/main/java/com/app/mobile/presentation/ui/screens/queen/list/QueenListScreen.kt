package com.app.mobile.presentation.ui.screens.queen.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.queen.QueenPreviewModel
import com.app.mobile.presentation.ui.components.*
import com.app.mobile.presentation.ui.screens.queen.list.models.QueenListActions
import com.app.mobile.presentation.ui.screens.queen.list.viewmodel.QueenListNavigationEvent
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

    // В HivesListScreen это в ViewModel, здесь пока сделаем локально для UI
    var selectedTab by remember { mutableIntStateOf(0) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        queenListViewModel.loadQueens()
    }

    ObserveAsEvents(queenListViewModel.event) { event ->
        when (event) {
            is QueenListNavigationEvent.NavigateToQueen -> onQueenClick(event.queenId)
            is QueenListNavigationEvent.NavigateToAddQueen -> onAddClick()
        }
    }

    when (val state = queenListUiState) {
        is QueenListUiState.Loading -> FullScreenProgressIndicator()

        is QueenListUiState.Error -> ErrorMessage(state.message, onRetry = { queenListViewModel.loadQueens() })

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
    actions: QueenListActions,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        stringResource(R.string.queens),
        stringResource(R.string.archive)
    )

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
                    onClick = actions.onAddClick,
                    icon = Icons.Filled.Add,
                    bottomPadding = Dimens.BottomAppBarHeight,
                    contentDescription = stringResource(R.string.add_queen)
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
                    if (queens.isNotEmpty()) {
                        val fabSpace = Dimens.BottomAppBarHeight + Dimens.FabSize + Dimens.ItemsSpacingLarge

                        QueensList(
                            queens = queens,
                            actions = actions,
                            bottomPadding = fabSpace
                        )
                    } else {
                        EmptyStub(text = stringResource(R.string.empty_queens_list_screen)) // "Список маток пуст"
                    }
                }
                1 -> {
                    // Заглушка для архива
                    EmptyStub(text = stringResource(R.string.empty_archive_list_screen))
                }
            }
        }
    }
}

@Composable
private fun QueensList(
    queens: List<QueenPreviewModel>,
    actions: QueenListActions,
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
        onClick = { onQueenClick(queen.id) },
        displayMode = QueenCardDisplayMode.SHOW_HIVE
    )
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
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}