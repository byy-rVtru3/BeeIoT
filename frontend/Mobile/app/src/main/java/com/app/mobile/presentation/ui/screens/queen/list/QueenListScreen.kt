package com.app.mobile.presentation.ui.screens.queen.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.app.mobile.presentation.models.queen.QueenPreviewModel
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.screens.queen.list.models.QueenListActions
import com.app.mobile.presentation.ui.screens.queen.list.viewmodel.QueenListNavigationEvent
import com.app.mobile.presentation.ui.screens.queen.list.viewmodel.QueenListUiState
import com.app.mobile.presentation.ui.screens.queen.list.viewmodel.QueenListViewModel
import com.app.mobile.ui.theme.Dimens
import kotlinx.coroutines.flow.collectLatest

@Composable
fun QueenListScreen(
    queenListViewModel: QueenListViewModel,
    onQueenClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val queenListUiState by queenListViewModel.queenListUiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                queenListViewModel.loadQueens()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(queenListViewModel.navigationEvent) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            queenListViewModel.navigationEvent.collectLatest { event ->
                when (event) {
                    is QueenListNavigationEvent.NavigateToQueen -> onQueenClick(event.queenId)
                    is QueenListNavigationEvent.NavigateToAddQueen -> onAddClick()
                }
            }
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
    Surface(modifier = Modifier.fillMaxSize().padding(bottom = Dimens.BottomAppBarHeight), color = MaterialTheme.colorScheme.background) {
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
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (queen.stage.isActionRequired) {
                    Text(
                        text = "!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = queen.stage.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
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
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}