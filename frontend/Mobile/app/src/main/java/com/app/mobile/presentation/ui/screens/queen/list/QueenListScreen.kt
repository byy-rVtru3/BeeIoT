package com.app.mobile.presentation.ui.screens.queen.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.mobile.presentation.models.queen.QueenPreviewModel
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.screens.queen.list.models.QueenListActions
import com.app.mobile.presentation.ui.screens.queen.list.viewmodel.QueenListNavigationEvent
import com.app.mobile.presentation.ui.screens.queen.list.viewmodel.QueenListUiState
import com.app.mobile.presentation.ui.screens.queen.list.viewmodel.QueenListViewModel

@Composable
fun QueenListScreen(
    queenListViewModel: QueenListViewModel,
    onQueenClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val queenListUiState by queenListViewModel.queenListUiState.observeAsState(QueenListUiState.Loading)

    LaunchedEffect(key1 = Unit) {
        queenListViewModel.loadQueens()
    }

    val navigationEvent by queenListViewModel.navigationEvent.observeAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { event ->
            when (event) {
                is QueenListNavigationEvent.NavigateToQueen -> onQueenClick(event.queenId)
                is QueenListNavigationEvent.NavigateToAddQueen -> onAddClick()
            }
            queenListViewModel.onNavigationHandled()
        }
    }

    when(val state = queenListUiState) {
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
fun QueenListContent(queens : List<QueenPreviewModel>, actions: QueenListActions) {
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

@Composable
fun QueenCard(queen: QueenPreviewModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = queen.name,
                    style = MaterialTheme.typography.titleMedium
                )
                if (queen.stage.isActionRequired) {
                    Text(
                        text = "!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = queen.stage.title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = queen.stage.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { queen.stage.progress },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = queen.stage.remainingDays,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}