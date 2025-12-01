package com.app.mobile.presentation.ui.screens.hives.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.mobile.R
import com.app.mobile.presentation.models.hive.HivePreview
import com.app.mobile.presentation.ui.components.CustomFloatingActionButton
import com.app.mobile.presentation.ui.components.EmptyScreen
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.screens.hives.list.models.HivesListActions
import com.app.mobile.presentation.ui.screens.hives.list.vewmodel.HivesListNavigationEvent
import com.app.mobile.presentation.ui.screens.hives.list.vewmodel.HivesListUiState
import com.app.mobile.presentation.ui.screens.hives.list.vewmodel.HivesListViewModel

@Composable
fun HivesListScreen(
    hivesListViewModel: HivesListViewModel,
    onHiveClick: (Int) -> Unit,
    onCreateHiveClick: () -> Unit
) {
    val hivesListUiState by hivesListViewModel.hivesListUiState.observeAsState(
        HivesListUiState
            .Loading
    )

    LaunchedEffect(Unit) {
        hivesListViewModel.loadHives()
    }

    val navigationEvent by hivesListViewModel.navigationEvent.observeAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { event ->
            when (event) {
                is HivesListNavigationEvent.NavigateToHive -> {
                    onHiveClick(event.hiveId)
                    hivesListViewModel.onNavigationHandled()
                }

                is HivesListNavigationEvent.NavigateToCreateHive -> {
                    onCreateHiveClick()
                    hivesListViewModel.onNavigationHandled()

                }
            }
        }
    }

    when (val state = hivesListUiState) {
        is HivesListUiState.Loading -> FullScreenProgressIndicator()

        is HivesListUiState.Error -> ErrorMessage(
            message = state.message,
            onRetry = hivesListViewModel::onRetry
        )

        is HivesListUiState.Empty -> EmptyHivesListScreen(hivesListViewModel::onCreateHiveClick)

        is HivesListUiState.Content -> {
            val actions = HivesListActions(
                onHiveClick = hivesListViewModel::onHiveClick,
                onCreateHiveClick = hivesListViewModel::onCreateHiveClick
            )
            HivesListContent(state.hives, actions)
        }
    }

}

@Composable
private fun HivesListContent(hives: List<HivePreview>, actions: HivesListActions) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Title("Список улеев", modifier = Modifier.padding(bottom = 16.dp))

        HivesList(hives, actions)
    }

    CreateButton(actions.onCreateHiveClick)
}

@Composable
private fun HivesList(hives: List<HivePreview>, actions: HivesListActions) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        items(hives) { hive ->
            HiveItem(hive, actions.onHiveClick)
        }
    }
}

@Composable
private fun HiveItem(hive: HivePreview, onHiveClick: (Int) -> Unit) {
    Card(
        modifier = Modifier.padding(5.dp),
        onClick = { onHiveClick(hive.id) })
    {
        Text(
            hive.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun EmptyHivesListScreen(onCreateHiveClick: () -> Unit) {
    EmptyScreen {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.empty_hives_list_screen))
        }
        CreateButton(onCreateHiveClick)
    }
}

@Composable
private fun CreateButton(onCreateHiveClick: () -> Unit) {
    CustomFloatingActionButton(
        onClick = onCreateHiveClick,
        icon = Icons.Filled.Add,
        contentDescription = "Добавить улей"
    )
}