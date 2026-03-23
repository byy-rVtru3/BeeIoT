package com.app.mobile.presentation.ui.screens.hub.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.hub.HubDetailUi
import com.app.mobile.presentation.ui.components.AppTopBar
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.InfoCard
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.components.SectionTitle
import com.app.mobile.presentation.ui.components.TopBarAction
import com.app.mobile.presentation.ui.screens.hub.details.models.HubActions
import com.app.mobile.presentation.ui.screens.hub.details.viewmodel.HubEvent
import com.app.mobile.presentation.ui.screens.hub.details.viewmodel.HubUiState
import com.app.mobile.presentation.ui.screens.hub.details.viewmodel.HubViewModel
import com.app.mobile.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubScreen(
	hubViewModel: HubViewModel,
	onHubListClick: () -> Unit,
	onHubEditClick: (hubId: String) -> Unit,
	onNotificationsClick: (hubId: String) -> Unit,
	onSensorChartClick: (hubId: String, sensorType: String, hubName: String, currentValue: Double?) -> Unit
) {
	val hubUiState by hubViewModel.uiState.collectAsStateWithLifecycle()
	val snackbarHostState = remember { SnackbarHostState() }
	val isRefreshing = (hubUiState as? HubUiState.Content)?.isRefreshing ?: false

	LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
		hubViewModel.loadHub()
	}

	ObserveAsEvents(hubViewModel.event) { event ->
		when (event) {
			is HubEvent.NavigateToHubList           -> onHubListClick()
			is HubEvent.NavigateToHubEdit           -> onHubEditClick(event.hubId)
			is HubEvent.NavigateToNotificationByHub -> onNotificationsClick(event.hubId)
			is HubEvent.NavigateToSensorChart       -> onSensorChartClick(event.hubId, event.sensorType, event.hubName, event.currentValue)
			is HubEvent.ShowSnackBar                -> snackbarHostState.showSnackbar(
				message = event.message,
				duration = SnackbarDuration.Short
			)
		}
	}

	PullToRefreshBox(
		isRefreshing = isRefreshing,
		onRefresh = hubViewModel::refresh
	) {
		when (val state = hubUiState) {
			is HubUiState.Loading -> FullScreenProgressIndicator()

			is HubUiState.Error   -> ErrorMessage(
				message = state.message,
				onRetry = hubViewModel::resetError
			)

			is HubUiState.Content -> {
				val actions = HubActions(
					onEditClick = hubViewModel::onEditClick,
					onDeleteClick = {}, // необходимо добавить удаление
					onTemperatureClick = hubViewModel::onTemperatureClick,
					onNoiseClick = hubViewModel::onNoiseClick,
					onWeightClick = hubViewModel::onWeightClick
				)
				HubContent(
					hub = state.hub,
					snackbarHostState = snackbarHostState,
					actions = actions,
					onBackClick = onHubListClick
				)
			}
		}
	}
}

@Composable
private fun HubContent(
	hub: HubDetailUi,
	snackbarHostState: SnackbarHostState,
	actions: HubActions,
	onBackClick: () -> Unit
) {
	Scaffold(
		topBar = {
			AppTopBar(
				title = stringResource(R.string.hub),
				onBackClick = onBackClick,
				action = TopBarAction.Delete(onClick = actions.onDeleteClick)
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

			// Основная информация
			Column(
				modifier = Modifier.fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
			) {
				SectionTitle(title = stringResource(R.string.section_main_info))
				Row(
					horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal),
					modifier = Modifier.fillMaxWidth()
				) {
					InfoCard(
						title = stringResource(R.string.label_name),
						value = hub.name,
						modifier = Modifier
							.weight(1f)
							.fillMaxWidth()
					)
				}
			}

			Column(
				modifier = Modifier.fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
			) {
				SectionTitle(title = stringResource(R.string.section_latest_readings))
				Row(
					horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal),
					modifier = Modifier.fillMaxWidth()
				) {
					val temp = hub.sensorReadings?.temperatureSensor
						?.let { stringResource(R.string.sensor_temperature_format, it.temperature.toFloat()) }
						?: stringResource(R.string.no)

					val noise = hub.sensorReadings?.noiseSensor
						?.let { stringResource(R.string.sensor_noise_format, it.noise.toFloat()) }
						?: stringResource(R.string.no)

					val weight = hub.sensorReadings?.weightSensor
						?.let { stringResource(R.string.sensor_weight_format, it.weight.toFloat()) }
						?: stringResource(R.string.no)

					InfoCard(
						title = stringResource(R.string.label_temperature),
						value = temp,
						modifier = Modifier
							.weight(1.5f)
							.clickable(onClick = actions.onTemperatureClick)
					)
					InfoCard(
						title = stringResource(R.string.label_noise),
						value = noise,
						modifier = Modifier
							.weight(1f)
							.clickable(onClick = actions.onNoiseClick)
					)
					InfoCard(
						title = stringResource(R.string.label_weight),
						value = weight,
						modifier = Modifier
							.weight(1f)
							.clickable(onClick = actions.onWeightClick)
					)
				}
			}

			Spacer(modifier = Modifier.height(Dimens.ItemsSpacingLarge))

			PrimaryButton(
				text = stringResource(R.string.edit),
				onClick = actions.onEditClick,
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}
