package com.app.mobile.presentation.ui.screens.queen.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.mobile.presentation.models.queen.QueenEditorModel
import com.app.mobile.presentation.ui.components.CustomTextField
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.screens.queen.editor.viewmodel.QueenEditorNavigationEvent
import com.app.mobile.presentation.ui.screens.queen.editor.viewmodel.QueenEditorUiState
import com.app.mobile.presentation.ui.screens.queen.editor.viewmodel.QueenEditorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QueenEditorScreen(
    queenEditorViewModel: QueenEditorViewModel,
    queenId: String?,
    onBackClick: () -> Unit
) {
    val queenUiState by queenEditorViewModel.queenEditorUiState.observeAsState(QueenEditorUiState.Loading)

    LaunchedEffect(key1 = Unit) {
        queenEditorViewModel.loadQueen(queenId)
    }

    val navigationEvent by queenEditorViewModel.navigationEvent.observeAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { event ->
            when (event) {
                is QueenEditorNavigationEvent.NavigateBack -> onBackClick()
            }
            queenEditorViewModel.onNavigationHandled()
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