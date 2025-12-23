package com.app.mobile.presentation.ui.screens.queen.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.presentation.models.queen.QueenEditorModel
import com.app.mobile.presentation.ui.components.CustomTextField
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.screens.queen.editor.viewmodel.QueenEditorNavigationEvent
import com.app.mobile.presentation.ui.screens.queen.editor.viewmodel.QueenEditorUiState
import com.app.mobile.presentation.ui.screens.queen.editor.viewmodel.QueenEditorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QueenEditorScreen(
    queenEditorViewModel: QueenEditorViewModel,
    onBackClick: () -> Unit
) {
    val queenUiState by queenEditorViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        queenEditorViewModel.loadQueen()
    }

    ObserveAsEvents(queenEditorViewModel.event) { event ->
        when (event) {
            is QueenEditorNavigationEvent.NavigateBack -> onBackClick()
        }
    }

    when (val state = queenUiState) {
        is QueenEditorUiState.Loading -> {
            FullScreenProgressIndicator()
        }

        is QueenEditorUiState.Error ->
            ErrorMessage(state.message, onRetry = {})

        is QueenEditorUiState.Content -> {
            QueenEditorContent(
                queenEditorModel = state.queenEditorModel,
                onNameChange = queenEditorViewModel::onNameChange,
                onDateChange = queenEditorViewModel::onDateChange,
                onHiveAdd = queenEditorViewModel::addHive,
                onSaveClick = queenEditorViewModel::onSaveClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueenEditorContent(
    queenEditorModel: QueenEditorModel,
    onNameChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onHiveAdd: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            CustomTextField(
                value = queenEditorModel.name,
                onValueChange = onNameChange,
                placeholder = "Имя матки"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date Picker Logic
            var showDatePicker by remember { mutableStateOf(false) }
            val datePickerState =
                rememberDatePickerState(initialSelectedDateMillis = queenEditorModel.birthDate)

            val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

            Box {
                CustomTextField(
                    value = dateFormatter.format(Date(queenEditorModel.birthDate)),
                    onValueChange = {},
                    placeholder = "Дата рождения"
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { onDateChange(it) }
                            showDatePicker = false
                        }) {
                            Text("OK", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hive Selection
            var expanded by remember { mutableStateOf(false) }
            val selectedHiveName =
                queenEditorModel.hives.find { it.id == queenEditorModel.hiveId }?.name
                    ?: "Выберите улей"

            Box {
                CustomTextField(
                    value = selectedHiveName,
                    onValueChange = {},
                    placeholder = "Улей"
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { expanded = true }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    queenEditorModel.hives.forEach { hive ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = hive.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onHiveAdd(hive.id)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Сохранить",
                onClick = onSaveClick
            )
        }
    }
}