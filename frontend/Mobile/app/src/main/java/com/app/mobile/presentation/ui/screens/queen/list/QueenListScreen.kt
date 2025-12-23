package com.app.mobile.presentation.ui.screens.queen.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.presentation.models.queen.QueenPreviewModel
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.QueenCard
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

        is QueenListUiState.Error -> ErrorMessage(state.message, onRetry = {})

        is QueenListUiState.Content -> {
            val actions = QueenListActions(
                onQueenClick = queenListViewModel::onQueenClick,
                onAddClick = queenListViewModel::onAddClick
            )
            QueenListContent(state.queens, actions)
        }
    }
}

@Composable
fun QueenListContent(queens: List<QueenPreviewModel>, actions: QueenListActions) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = Dimens.BottomAppBarHeight),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(queens) { queen ->
                    QueenCard(queen = queen, onClick = { actions.onQueenClick(queen.id) })
                }
            }

            FloatingActionButton(
                onClick = actions.onAddClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Queen")
            }
        }
    }
}

