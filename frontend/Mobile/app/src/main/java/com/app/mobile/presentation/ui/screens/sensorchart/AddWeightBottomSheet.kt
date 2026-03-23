package com.app.mobile.presentation.ui.screens.sensorchart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import com.app.mobile.R
import com.app.mobile.ui.theme.Dimens
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private enum class DateTimePickerStep { NONE,
	DATE,
	TIME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWeightBottomSheet(
	isSubmitting: Boolean,
	onConfirm: (weight: Double, dateTime: LocalDateTime) -> Unit,
	onDismiss: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

	ModalBottomSheet(
		onDismissRequest = onDismiss,
		sheetState = sheetState
	) {
		AddWeightBottomSheetContent(
			isSubmitting = isSubmitting,
			onConfirm = onConfirm,
			onDismiss = onDismiss
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWeightBottomSheetContent(
	isSubmitting: Boolean,
	onConfirm: (weight: Double, dateTime: LocalDateTime) -> Unit,
	onDismiss: () -> Unit
) {
	var weightInput by remember { mutableStateOf("") }
	var selectedDate by remember { mutableStateOf(LocalDate.now()) }
	var selectedTime by remember { mutableStateOf(LocalTime.now()) }
	var pickerStep by remember { mutableStateOf(DateTimePickerStep.NONE) }

	val dateTimeFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }
	val formattedDateTime = LocalDateTime.of(selectedDate, selectedTime).format(dateTimeFormatter)

	val weightValue = weightInput.replace(",", ".").toDoubleOrNull()
	val canConfirm = weightValue != null && !isSubmitting

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = Dimens.ScreenContentPadding)
			.padding(bottom = Dimens.ItemsSpacingLarge),
		verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
	) {
		Text(
			text = stringResource(R.string.add_weight_title),
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface
		)

		OutlinedTextField(
			value = weightInput,
			onValueChange = { weightInput = it },
			label = { Text(stringResource(R.string.weight_value_label)) },
			placeholder = { Text(stringResource(R.string.weight_value_placeholder)) },
			keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
			singleLine = true,
			modifier = Modifier.fillMaxWidth()
		)

		// Поле выбора даты и времени
		Text(
			text = stringResource(R.string.weight_date_time_label),
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.clickable { pickerStep = DateTimePickerStep.DATE },
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {

			Surface(
				shape = RoundedCornerShape(Dimens.ItemCardRadius),
				color = MaterialTheme.colorScheme.surface,
				border = BorderStroke(Dimens.BorderWidthNormal, MaterialTheme.colorScheme.primary)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.padding(
						start = Dimens.ItemCardPadding,
						top = Dimens.ItemCardTextPadding,
						bottom = Dimens.ItemCardTextPadding,
						end = Dimens.Null
					)
				) {
					Text(
						text = formattedDateTime,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier.padding(Dimens.ItemCardTextPadding)
					)
					Spacer(Modifier.weight(1f))
					Icon(
						imageVector = Icons.Default.DateRange,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurface,
						modifier = Modifier.padding(Dimens.ItemCardTextPadding)
					)
				}
			}
		}

		Spacer(modifier = Modifier.height(Dimens.ItemsSpacingSmall))

		Button(
			onClick = { onConfirm(weightValue!!, LocalDateTime.of(selectedDate, selectedTime)) },
			enabled = canConfirm,
			modifier = Modifier.fillMaxWidth()
		) {
			Text(stringResource(R.string.save))
		}

		TextButton(
			onClick = onDismiss,
			modifier = Modifier.fillMaxWidth()
		) {
			Text(stringResource(R.string.cancel))
		}
	}

	// Последовательный пикер: сначала дата, затем время
	when (pickerStep) {
		DateTimePickerStep.DATE -> {
			val datePickerState = rememberDatePickerState(
				initialSelectedDateMillis = selectedDate
					.atStartOfDay(ZoneOffset.UTC)
					.toInstant()
					.toEpochMilli()
			)
			DatePickerDialog(
				onDismissRequest = { pickerStep = DateTimePickerStep.NONE },
				confirmButton = {
					TextButton(onClick = {
						datePickerState.selectedDateMillis?.let { millis ->
							selectedDate = Instant.ofEpochMilli(millis)
								.atZone(ZoneOffset.UTC)
								.toLocalDate()
						}
						pickerStep = DateTimePickerStep.TIME
					}) {
						Text(stringResource(R.string.ok))
					}
				},
				dismissButton = {
					TextButton(onClick = { pickerStep = DateTimePickerStep.NONE }) {
						Text(stringResource(R.string.cancel))
					}
				}
			) {
				DatePicker(state = datePickerState)
			}
		}

		DateTimePickerStep.TIME -> {
			val timePickerState = rememberTimePickerState(
				initialHour = selectedTime.hour,
				initialMinute = selectedTime.minute,
				is24Hour = true
			)
			Dialog(onDismissRequest = { pickerStep = DateTimePickerStep.NONE }) {
				Surface(
					shape = RoundedCornerShape(Dimens.ItemCardRadius),
					color = MaterialTheme.colorScheme.surface
				) {
					Column(
						modifier = Modifier.padding(Dimens.ScreenContentPadding),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						TimePicker(state = timePickerState)
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.End
						) {
							TextButton(onClick = { pickerStep = DateTimePickerStep.NONE }) {
								Text(stringResource(R.string.cancel))
							}
							TextButton(onClick = {
								selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
								pickerStep = DateTimePickerStep.NONE
							}) {
								Text(stringResource(R.string.ok))
							}
						}
					}
				}
			}
		}

		DateTimePickerStep.NONE -> Unit
	}
}
