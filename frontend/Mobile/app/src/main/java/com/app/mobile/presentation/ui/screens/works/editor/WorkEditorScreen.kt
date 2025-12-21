package com.app.mobile.presentation.ui.screens.works.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.presentation.models.hive.WorkUi
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.screens.works.editor.models.WorksEditorActions
import com.app.mobile.presentation.ui.screens.works.editor.viewmodel.WorksEditorNavigationEvent
import com.app.mobile.presentation.ui.screens.works.editor.viewmodel.WorksEditorUiState
import com.app.mobile.presentation.ui.screens.works.editor.viewmodel.WorksEditorViewModel

@Composable
fun WorksEditorScreen(
    worksEditorViewModel: WorksEditorViewModel,
    onBackClick: () -> Unit
) {
    val workEditorUiState by worksEditorViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        worksEditorViewModel.loadWork()
    }

    ObserveAsEvents(worksEditorViewModel.event) { event ->
        when (event) {
            is WorksEditorNavigationEvent.NavigateToWorksList -> onBackClick()
        }
    }

    when (val state = workEditorUiState) {
        is WorksEditorUiState.Loading -> FullScreenProgressIndicator()
        is WorksEditorUiState.Error -> ErrorMessage(state.message, onRetry = {})
        is WorksEditorUiState.Content -> {
            val actions = WorksEditorActions(
                onTitleChange = worksEditorViewModel::onTitleChange,
                onTextChange = worksEditorViewModel::onTextChange,
                onSaveClick = worksEditorViewModel::onSaveClick
            )
            WorksEditorContent(
                state.work, actions, worksEditorViewModel::onSaveClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksEditorContent(
    work: WorkUi,
    actions: WorksEditorActions,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (work.id.isEmpty()) "Создание работы" else "Редактирование работы")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = work.title,
                onValueChange = actions.onTitleChange,
                label = { Text("Заголовок") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = work.text,
                onValueChange = actions.onTextChange,
                label = { Text("Описание") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Button(
                onClick = actions.onSaveClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сохранить")
            }
        }
    }
}