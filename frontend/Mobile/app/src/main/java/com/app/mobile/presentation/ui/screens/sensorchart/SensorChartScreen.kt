package com.app.mobile.presentation.ui.screens.sensorchart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.domain.models.telemetry.SensorType
import com.app.mobile.domain.models.telemetry.TelemetryDataPoint
import com.app.mobile.presentation.ui.components.AppTopBar
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.InfoCard
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.SectionTitle
import com.app.mobile.presentation.ui.screens.sensorchart.viewmodel.SensorChartEvent
import com.app.mobile.presentation.ui.screens.sensorchart.viewmodel.SensorChartUiState
import com.app.mobile.presentation.ui.screens.sensorchart.viewmodel.SensorChartViewModel
import com.app.mobile.ui.theme.Dimens
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisTickComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun SensorChartScreen(
	viewModel: SensorChartViewModel,
	onBackClick: () -> Unit
) {
	val uiState by viewModel.uiState.collectAsStateWithLifecycle()
	val isSubmitting by viewModel.isSubmittingWeight.collectAsStateWithLifecycle()
	val snackbarHostState = remember { SnackbarHostState() }
	var showWeightSheet by remember { mutableStateOf(false) }

	val title = when (viewModel.sensorType) {
		SensorType.TEMPERATURE -> stringResource(R.string.chart_title_temperature)
		SensorType.NOISE       -> stringResource(R.string.chart_title_noise)
		SensorType.WEIGHT      -> stringResource(R.string.chart_title_weight)
	}

	ObserveAsEvents(viewModel.event) { event ->
		when (event) {
			is SensorChartEvent.ShowSnackBar      -> snackbarHostState.showSnackbar(
				message = event.message,
				duration = SnackbarDuration.Short
			)
			is SensorChartEvent.WeightAddedSuccessfully -> {
				showWeightSheet = false
				snackbarHostState.showSnackbar(
					message = "Показание успешно добавлено",
					duration = SnackbarDuration.Short
				)
			}
		}
	}

	Scaffold(
		topBar = {
			AppTopBar(
				title = title,
				onBackClick = onBackClick
			)
		},
		snackbarHost = { SnackbarHost(snackbarHostState) },
		containerColor = MaterialTheme.colorScheme.surfaceVariant,
		floatingActionButton = {
			if (viewModel.sensorType == SensorType.WEIGHT) {
				FloatingActionButton(
					onClick = { showWeightSheet = true },
					shape = CircleShape,
					containerColor = MaterialTheme.colorScheme.background,
					contentColor = MaterialTheme.colorScheme.onBackground
				) {
					Icon(
						imageVector = Icons.Default.Add,
						contentDescription = stringResource(R.string.add_weight_record)
					)
				}
			}
		}
	) { innerPadding ->
		when (val state = uiState) {
			is SensorChartUiState.Loading -> FullScreenProgressIndicator()

			is SensorChartUiState.Error   -> ErrorMessage(
				message = state.message,
				onRetry = viewModel::resetError
			)

			is SensorChartUiState.Content -> {
				SensorChartContent(
					state = state,
					onDateSelected = { viewModel.updateSince(it) },
					modifier = Modifier
						.padding(innerPadding)
						.fillMaxSize()
						.padding(Dimens.ScreenContentPadding)
				)
			}
		}
	}

	if (showWeightSheet) {
		AddWeightBottomSheet(
			isSubmitting = isSubmitting,
			onConfirm = { weight, dateTime -> viewModel.submitWeightRecord(weight, dateTime) },
			onDismiss = { showWeightSheet = false }
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SensorChartContent(
	state: SensorChartUiState.Content,
	onDateSelected: (LocalDate) -> Unit,
	modifier: Modifier = Modifier
) {
	var showDatePicker by remember { mutableStateOf(false) }

	val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
	val sinceFormatted = state.since.format(dateFormatter)

	val currentValueLabel = when (state.sensorType) {
		SensorType.TEMPERATURE -> stringResource(R.string.chart_current_temperature)
		SensorType.NOISE       -> stringResource(R.string.chart_current_noise)
		SensorType.WEIGHT      -> stringResource(R.string.chart_current_weight)
	}

	val periodLabel = when (state.sensorType) {
		SensorType.TEMPERATURE -> stringResource(R.string.chart_period_temperature)
		SensorType.NOISE       -> stringResource(R.string.chart_period_noise)
		SensorType.WEIGHT      -> stringResource(R.string.chart_period_weight)
	}

	val currentValueFormatted = when (state.sensorType) {
		SensorType.TEMPERATURE -> state.currentValue?.let {
			stringResource(R.string.sensor_temperature_format, it.toString())
		} ?: "-"

		SensorType.NOISE       -> state.currentValue?.let {
			stringResource(R.string.sensor_noise_format, it.toString())
		} ?: "-"

		SensorType.WEIGHT      -> state.currentValue?.let {
			stringResource(R.string.sensor_weight_format, it.toString())
		} ?: "-"
	}

	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingLarge)
	) {
		// Основная информация
		Column(
			modifier = Modifier.fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingSmall)
		) {
			SectionTitle(title = stringResource(R.string.section_main_info))
			InfoCard(
				title = stringResource(R.string.hub),
				value = state.hubName,
				modifier = Modifier.fillMaxWidth()
			)
		}

		// Текущее значение
		InfoCard(
			title = currentValueLabel,
			value = currentValueFormatted,
			modifier = Modifier.fillMaxWidth()
		)

		// Выбор периода
		Text(
			text = periodLabel,
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
		)
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.clickable { showDatePicker = true },
			horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal),
			verticalAlignment = Alignment.CenterVertically
		) {
			Surface(
				shape = RoundedCornerShape(Dimens.ItemCardRadius),
				color = MaterialTheme.colorScheme.surface,
				border = BorderStroke(Dimens.BorderWidthNormal, MaterialTheme.colorScheme.primary)
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.padding(
							start = Dimens.Null,
							end = Dimens.Null,
							top = Dimens.ItemCardTextPadding,
							bottom = Dimens.ItemCardTextPadding
						)
						.fillMaxWidth()
				) {
					Text(
						text = sinceFormatted,
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

		// График
		if (state.dataPoints.isEmpty()) {
			Column(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.Center
			) {
				Text(
					text = stringResource(R.string.chart_no_data),
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		} else {
			SensorChart(
				dataPoints = state.dataPoints,
				sensorType = state.sensorType,
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
			)
		}
	}

	if (showDatePicker) {
		val initialMillis = state.since
			.atStartOfDay(ZoneOffset.UTC)
			.toInstant()
			.toEpochMilli()
		val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

		DatePickerDialog(
			onDismissRequest = { showDatePicker = false },
			confirmButton = {
				TextButton(onClick = {
					showDatePicker = false
					datePickerState.selectedDateMillis?.let { millis ->
						val date = Instant.ofEpochMilli(millis)
							.atZone(ZoneOffset.UTC)
							.toLocalDate()
						onDateSelected(date)
					}
				}) {
					Text(stringResource(R.string.ok))
				}
			},
			dismissButton = {
				TextButton(onClick = { showDatePicker = false }) {
					Text(stringResource(R.string.cancel))
				}
			}
		) {
			DatePicker(state = datePickerState)
		}
	}
}

@Composable
private fun SensorChart(
	dataPoints: List<TelemetryDataPoint>,
	sensorType: SensorType,
	modifier: Modifier = Modifier
) {
	val modelProducer = remember { CartesianChartModelProducer() }

	val timeFormatter = remember {
		SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
	}

	val timestamps = remember(dataPoints) {
		dataPoints.map { it.time }
	}

	val bottomAxisValueFormatter = remember(timestamps) {
		CartesianValueFormatter { _, value, _ ->
			val index = value.toInt()
			if (index in timestamps.indices) {
				timeFormatter.format(Date(timestamps[index] * 1000))
			} else {
				""
			}
		}
	}

	androidx.compose.runtime.LaunchedEffect(dataPoints) {
		modelProducer.runTransaction {
			lineSeries {
				series(dataPoints.map { it.value })
			}
		}
	}

	val lineColor = when (sensorType) {
		SensorType.TEMPERATURE -> MaterialTheme.colorScheme.error
		SensorType.NOISE       -> MaterialTheme.colorScheme.tertiary
		SensorType.WEIGHT      -> MaterialTheme.colorScheme.primary
	}

	val axisColor = MaterialTheme.colorScheme.onSurface
	val axisLine = rememberAxisLineComponent(fill = fill(axisColor))
	val axisTick = rememberAxisTickComponent(fill = fill(axisColor))
	val axisLabel = rememberAxisLabelComponent(color = axisColor)

	CartesianChartHost(
		chart = rememberCartesianChart(
			rememberLineCartesianLayer(
				lineProvider = LineCartesianLayer.LineProvider.series(
					LineCartesianLayer.Line(fill = LineCartesianLayer.LineFill.single(fill(lineColor)))
				),
				rangeProvider = remember {
					object : CartesianLayerRangeProvider {
						override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
							val padding = (maxY - minY) * 0.2
							return minY - padding
						}

						override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
							val padding = (maxY - minY) * 0.2
							return maxY + padding
						}
					}
				}
			),
			startAxis = VerticalAxis.rememberStart(
				line = axisLine,
				tick = axisTick,
				label = axisLabel
			),
			bottomAxis = HorizontalAxis.rememberBottom(
				line = axisLine,
				tick = axisTick,
				label = axisLabel,
				valueFormatter = bottomAxisValueFormatter
			),
		),
		modelProducer = modelProducer,
		modifier = modifier
	)
}