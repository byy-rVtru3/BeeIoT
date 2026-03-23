package com.app.mobile.presentation.ui.screens.queen.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.queen.QueenEditorModel
import com.app.mobile.presentation.ui.components.AppTopBar
import com.app.mobile.presentation.ui.components.CustomTextField
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.screens.queen.editor.viewmodel.QueenEditorEvent
import com.app.mobile.presentation.ui.screens.queen.editor.viewmodel.QueenEditorUiState
import com.app.mobile.presentation.ui.screens.queen.editor.viewmodel.QueenEditorViewModel
import com.app.mobile.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QueenEditorScreen(
	queenEditorViewModel: QueenEditorViewModel,
	onBackClick: () -> Unit
) {
	val queenUiState by queenEditorViewModel.uiState.collectAsStateWithLifecycle()
	val snackbarHostState = remember { SnackbarHostState() }

	LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
		queenEditorViewModel.loadQueen()
	}

	ObserveAsEvents(queenEditorViewModel.event) { event ->
		when (event) {
			is QueenEditorEvent.NavigateBack -> onBackClick()

			is QueenEditorEvent.ShowSnackBar -> {
				snackbarHostState.showSnackbar(
					event.message,
					duration = SnackbarDuration.Short
				)
			}
		}
	}

	when (val state = queenUiState) {
		is QueenEditorUiState.Loading -> FullScreenProgressIndicator()
		is QueenEditorUiState.Error   -> ErrorMessage(state.message, onRetry = queenEditorViewModel::resetError)

		is QueenEditorUiState.Content -> {
			QueenEditorContent(
				queenEditorModel = state.queenEditorModel,
				snackbarHostState = snackbarHostState,
				onNameChange = queenEditorViewModel::onNameChange,
				onDateChange = queenEditorViewModel::onDateChange,
				onSaveClick = queenEditorViewModel::onSaveClick,
				onBackClick = onBackClick
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueenEditorContent(
	queenEditorModel: QueenEditorModel,
	snackbarHostState: SnackbarHostState,
	onNameChange: (String) -> Unit,
	onDateChange: (Long) -> Unit,
	onSaveClick: () -> Unit,
	onBackClick: () -> Unit
) {
	Scaffold(
		topBar = {
			AppTopBar(
				title = stringResource(R.string.queen_edit_title), // "Добавление/Редактирование матки"
				onBackClick = onBackClick
			)
		},
		snackbarHost = { SnackbarHost(snackbarHostState) },
		containerColor = MaterialTheme.colorScheme.surfaceVariant
	) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
				.padding(Dimens.ScreenContentPadding),
			verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingLarge)
		) {

			// --- Поле ввода имени ---
			Column(verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)) {
				Text(
					text = stringResource(R.string.queen_name_label), // "Имя матки"
					style = MaterialTheme.typography.labelLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				CustomTextField(
					value = queenEditorModel.name,
					onValueChange = onNameChange,
					placeholder = stringResource(R.string.queen_name_placeholder),
					modifier = Modifier.fillMaxWidth()
				)
			}

			// --- Выбор даты рождения ---
			val datePickerState = rememberDatePickerState(initialSelectedDateMillis = queenEditorModel.birthDate)
			var showDatePicker by remember { mutableStateOf(false) }
			val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

			Column(verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)) {
				Text(
					text = stringResource(R.string.birth_date_label), // "Дата рождения"
					style = MaterialTheme.typography.labelLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				Box(modifier = Modifier.fillMaxWidth()) {
					CustomTextField(
						value = dateFormatter.format(Date(queenEditorModel.birthDate)),
						onValueChange = {},
						placeholder = "",
						trailingIcon = {
							Icon(Icons.Rounded.CalendarToday, contentDescription = null)
						},
						modifier = Modifier.fillMaxWidth()
					)
					// Прозрачный слой поверх для клика
					Box(
						modifier = Modifier
							.matchParentSize()
							.clickable { showDatePicker = true }
					)
				}
			}

			if (showDatePicker) {
				DatePickerDialog(
					onDismissRequest = { showDatePicker = false },
					confirmButton = {
						TextButton(onClick = {
							datePickerState.selectedDateMillis?.let { onDateChange(it) }
							showDatePicker = false
						}) {
							Text(stringResource(R.string.ok), color = MaterialTheme.colorScheme.primary)
						}
					},
					dismissButton = {
						TextButton(onClick = { showDatePicker = false }) {
							Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary)
						}
					}
				) {
					DatePicker(state = datePickerState)
				}
			}

			Spacer(modifier = Modifier.weight(1f))

			PrimaryButton(
				text = stringResource(R.string.save),
				onClick = onSaveClick,
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}

