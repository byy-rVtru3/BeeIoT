package com.app.mobile.presentation.ui.screens.works.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.app.mobile.presentation.models.hive.WorkUi
import com.app.mobile.presentation.ui.components.AppTopBar
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.components.TopBarAction
import com.app.mobile.presentation.ui.screens.works.detail.viewmodel.WorkDetailEvent
import com.app.mobile.presentation.ui.screens.works.detail.viewmodel.WorkDetailUiState
import com.app.mobile.presentation.ui.screens.works.detail.viewmodel.WorkDetailViewModel
import com.app.mobile.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkDetailScreen(
    workDetailViewModel: WorkDetailViewModel,
    onEditClick: (workId: String, hiveId: String) -> Unit,
    onBackClick: () -> Unit,
) {
    val uiState by workDetailViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isRefreshing = (uiState as? WorkDetailUiState.Content)?.isRefreshing ?: false

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        workDetailViewModel.loadWork()
    }

    ObserveAsEvents(workDetailViewModel.event) { event ->
        when (event) {
            is WorkDetailEvent.NavigateToEdit -> onEditClick(event.workId, event.hiveId)
            is WorkDetailEvent.NavigateBack   -> onBackClick()
            is WorkDetailEvent.ShowSnackBar   -> snackbarHostState.showSnackbar(
                message = event.message,
                duration = SnackbarDuration.Short
            )
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = workDetailViewModel::refresh
    ) {
        when (val state = uiState) {
            is WorkDetailUiState.Loading -> FullScreenProgressIndicator()
            is WorkDetailUiState.Error   -> ErrorMessage(state.message, workDetailViewModel::resetError)
            is WorkDetailUiState.Content -> WorkDetailContent(
                work = state.work,
                snackbarHostState = snackbarHostState,
                onEditClick = workDetailViewModel::onEditClick,
                onDeleteClick = workDetailViewModel::onDeleteClick,
                onBackClick = onBackClick,
            )
        }
    }
}

@Composable
private fun WorkDetailContent(
    work: WorkUi,
    snackbarHostState: SnackbarHostState,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.work_detail_title),
                onBackClick = onBackClick,
                action = TopBarAction.Delete(onClick = onDeleteClick)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenContentPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = work.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = work.dateTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(Dimens.ItemSpacingNormal))

            Text(
                text = stringResource(R.string.work_hive_format, work.hiveId),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.ItemSpacingNormal))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(Dimens.ItemsSpacingLarge))

            Text(
                text = work.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = stringResource(R.string.edit),
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Dimens.ButtonSoloVerticalPadding)
            )
        }
    }
}
