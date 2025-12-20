package com.app.mobile.presentation.ui.screens.hive.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.hive.HivePreview
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.SelectorTopBar
import com.app.mobile.presentation.ui.screens.hive.list.models.HivesListActions
import com.app.mobile.presentation.ui.screens.hive.list.vewmodel.HivesListNavigationEvent
import com.app.mobile.presentation.ui.screens.hive.list.vewmodel.HivesListUiState
import com.app.mobile.presentation.ui.screens.hive.list.vewmodel.HivesListViewModel
import com.app.mobile.ui.theme.Dimens
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HivesListScreen(
    hivesListViewModel: HivesListViewModel,
    onHiveClick: (String) -> Unit,
    onCreateHiveClick: () -> Unit
) {
    val hivesListUiState by hivesListViewModel.hivesListUiState.collectAsStateWithLifecycle()

    val selectedTab by hivesListViewModel.selectedTab.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hivesListViewModel.loadHives()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(hivesListViewModel.navigationEvent) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            hivesListViewModel.navigationEvent.collectLatest { event ->
                when (event) {
                    is HivesListNavigationEvent.NavigateToHive -> {
                        onHiveClick(event.hiveId)
                    }

                    is HivesListNavigationEvent.NavigateToCreateHive -> {
                        onCreateHiveClick()
                    }
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
            HivesListContent(
                state.hives, actions,
                selectedTab = selectedTab,
                onTabSelected = hivesListViewModel::onTabSelected
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HivesListContent(
    hives: List<HivePreview>, actions: HivesListActions, selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(stringResource(R.string.active_hives),stringResource(R.string.archive))

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
                FloatingActionButton(
                    onClick = actions.onCreateHiveClick,
                    modifier = Modifier.padding(bottom = Dimens.BottomAppBarHeight),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_hive))
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> {
                    // Вкладка 1: Список ульев
                    if (hives.isNotEmpty()) {
                        HivesList(
                            hives = hives,
                            actions = actions
                        )
                    } else {
                        // Если список пуст, показываем заглушку внутри таба
                        EmptyStub(text = stringResource(R.string.empty_hives_list_screen))
                    }
                }
                1 -> {
                    // Вкладка 2: ЗАГЛУШКА ДЛЯ АРХИВА
                    EmptyStub(text = "В архиве пока пусто")
                }
            }
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = Dimens.ScreenContentPadding)
    ) {
        items(hives) { hive ->
            HiveItem(hive, actions.onHiveClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HiveItem(hive: HivePreview, onHiveClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = { onHiveClick(hive.id) }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = hive.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmptyHivesListScreen(onCreateHiveClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Список ульев") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateHiveClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить улей")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.empty_hives_list_screen),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}