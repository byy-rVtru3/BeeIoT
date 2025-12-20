package com.app.mobile.presentation.ui.screens.hive.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.app.mobile.presentation.models.hive.HiveEditorModel
import com.app.mobile.presentation.ui.components.CustomTextField
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.screens.hive.editor.models.HiveEditorActions
import com.app.mobile.presentation.ui.screens.hive.editor.viewmodel.HiveEditorNavigationEvent
import com.app.mobile.presentation.ui.screens.hive.editor.viewmodel.HiveEditorUiState
import com.app.mobile.presentation.ui.screens.hive.editor.viewmodel.HiveEditorViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HiveEditorScreen(
    hiveEditorViewModel: HiveEditorViewModel,
    onBackClick: () -> Unit,
    onCreateQueenClick: () -> Unit,
    onCreateHubClick: () -> Unit
) {
    val hiveEditorUiState by hiveEditorViewModel.hiveEditorUiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hiveEditorViewModel.loadHive()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(hiveEditorViewModel.navigationEvent) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            hiveEditorViewModel.navigationEvent.collectLatest { event ->
                when (event) {
                    is HiveEditorNavigationEvent.NavigateToCreateQueen -> onCreateQueenClick()
                    is HiveEditorNavigationEvent.NavigateToCreateHub -> onCreateHubClick()
                    is HiveEditorNavigationEvent.NavigateBack -> onBackClick()
                }
            }
        }
    }

    when (val state = hiveEditorUiState) {
        is HiveEditorUiState.Loading -> FullScreenProgressIndicator()
        is HiveEditorUiState.Error -> ErrorMessage(state.message, onRetry = {})
        is HiveEditorUiState.Content -> {
            val actions = HiveEditorActions(
                onNameChange = hiveEditorViewModel::onNameChange,
                onCreateQueenClick = hiveEditorViewModel::onCreateQueenClick,
                onCreateHubClick = hiveEditorViewModel::onCreateHubClick,
                onAddQueenClick = hiveEditorViewModel::onQueenAdd,
                onAddHubClick = hiveEditorViewModel::onHubAdd,
                onSaveClick = hiveEditorViewModel::onSaveClick
            )
            HiveEditorContent(state.hiveEditorModel, actions)
        }
    }
}

@Composable
fun HiveEditorContent(hiveEditorModel: HiveEditorModel, actions: HiveEditorActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CustomTextField(
            value = hiveEditorModel.name,
            onValueChange = actions.onNameChange,
            placeholder = "Hive Name"
        )

        SelectionSection(
            title = "Hub",
            items = hiveEditorModel.hubs.map { it.id to it.name },
            selectedId = hiveEditorModel.connectedHubId,
            onItemSelected = actions.onAddHubClick,
            onCreateClick = actions.onCreateHubClick,
            createLabel = "Create new Hub"
        )

        SelectionSection(
            title = "Queen",
            items = hiveEditorModel.queens.map { it.id to it.name },
            selectedId = hiveEditorModel.connectedQueenId,
            onItemSelected = actions.onAddQueenClick,
            onCreateClick = actions.onCreateQueenClick,
            createLabel = "Create new Queen"
        )

        PrimaryButton(
            text = "Сохранить",
            onClick = actions.onSaveClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSection(
    title: String,
    items: List<Pair<String, String>>,
    selectedId: String?,
    onItemSelected: (String) -> Unit,
    onCreateClick: () -> Unit,
    createLabel: String
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = items.find { it.first == selectedId }?.second ?: "Select $title"

    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item.second,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            onItemSelected(item.first)
                            expanded = false
                        }
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Text(
                            text = createLabel,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    onClick = {
                        onCreateClick()
                        expanded = false
                    }
                )
            }
        }
    }
}