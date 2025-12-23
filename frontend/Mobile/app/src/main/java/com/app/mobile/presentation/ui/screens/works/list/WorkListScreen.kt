package com.app.mobile.presentation.ui.screens.works.list

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.hive.WorkUi
import com.app.mobile.presentation.ui.components.*
import com.app.mobile.presentation.ui.screens.works.list.viewmodel.WorksListNavigationEvent
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

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        worksListViewModel.loadWorks()
    }

    ObserveAsEvents(worksListViewModel.event) { event ->
        when (event) {
            is WorksListNavigationEvent.NavigateToWorkEditor -> onWorkClick(
                event.workId,
                event.hiveId
            )
            is WorksListNavigationEvent.NavigateToWorkCreate -> onCreateClick(event.hiveId)
            is WorksListNavigationEvent.NavigateBack -> onBackClick()
        }
    }

    when (val state = worksUiState) {
        is WorksListUiState.Loading -> FullScreenProgressIndicator()
        is WorksListUiState.Error -> ErrorMessage(state.message, onRetry = {})
        is WorksListUiState.Content -> WorksListContent(
            state.works,
            worksListViewModel::onWorkClick,
            worksListViewModel::onCreateClick,
            onNavigateBack = onBackClick
        )
    }
}

@Composable
fun WorksListContent(
    works: List<WorkUi>,
    onWorkClick: (String) -> Unit,
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
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        floatingActionButton = {
            CustomFloatingActionButton(
                onClick = onCreateClick,
                icon = Icons.Filled.Add,
                bottomPadding = Dimens.Null,
                contentDescription = stringResource(R.string.add)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
        ) {
            if (works.isNotEmpty()) {
                val fabSpace = Dimens.FabSize + Dimens.ItemsSpacingLarge

                WorksList(
                    works = works,
                    onWorkClick = onWorkClick,
                    bottomPadding = fabSpace
                )
            } else {
                EmptyStub(text = stringResource(R.string.empty_works_list))
            }
        }
    }
}

@Composable
private fun WorksList(
    works: List<WorkUi>,
    onWorkClick: (String) -> Unit,
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
        items(
            items = works,
            key = { it.id }
        ) { work ->
            DetailsItemCard(
                title = work.title,
                description = work.text,
                footer = work.dateTime,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onWorkClick(work.id) }
            )
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