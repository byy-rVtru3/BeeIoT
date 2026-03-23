package com.app.mobile.presentation.ui.screens.hive.details

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
import com.app.mobile.presentation.models.hive.HiveUi
import com.app.mobile.presentation.models.hive.WorkUi
import com.app.mobile.presentation.ui.components.AppTopBar
import com.app.mobile.presentation.ui.components.WorkTileCard
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.InfoCard
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.components.QueenCard
import com.app.mobile.presentation.ui.components.QueenCardDisplayMode
import com.app.mobile.presentation.ui.components.SectionHeaderWithAction
import com.app.mobile.presentation.ui.components.SectionTitle
import com.app.mobile.presentation.ui.components.TopBarAction
import com.app.mobile.presentation.ui.screens.hive.details.models.HiveActions
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveEvent
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveUiState
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveViewModel
import com.app.mobile.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveScreen(
	hiveViewModel: HiveViewModel,
	onQueenClick: (queenName: String) -> Unit,
	onWorksClick: (hiveName: String) -> Unit,
	onWorkDetailClick: (workId: String, hiveName: String) -> Unit,
	onNotificationsClick: (hiveName: String) -> Unit,
	onTemperatureClick: (hubId: String, hubName: String, currentValue: Double?) -> Unit,
	onNoiseClick: (hubId: String, hubName: String, currentValue: Double?) -> Unit,
	onWeightClick: (hubId: String, hubName: String, currentValue: Double?) -> Unit,
	onHiveListClick: () -> Unit,
	onHiveEditClick: (hiveName: String) -> Unit
) {
	val hiveUiState by hiveViewModel.uiState.collectAsStateWithLifecycle()
	val snackbarHostState = remember { SnackbarHostState() }
	val isRefreshing = (hiveUiState as? HiveUiState.Content)?.isRefreshing ?: false

	LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
		hiveViewModel.loadHive()
	}

	ObserveAsEvents(hiveViewModel.event) { event ->
		when (event) {
			is HiveEvent.NavigateToHiveList           -> onHiveListClick()
			is HiveEvent.NavigateToQueenByHive        -> onQueenClick(event.queenName)
			is HiveEvent.NavigateToWorkByHive         -> onWorksClick(event.hiveName)
			is HiveEvent.NavigateToWorkDetail         -> onWorkDetailClick(event.workId, event.hiveName)
			is HiveEvent.NavigateToNotificationByHive -> onNotificationsClick(event.hiveName)
			is HiveEvent.NavigateToTemperatureByHive  -> onTemperatureClick(event.hubId, event.hubName, event.currentValue)
			is HiveEvent.NavigateToNoiseByHive        -> onNoiseClick(event.hubId, event.hubName, event.currentValue)
			is HiveEvent.NavigateToWeightByHive       -> onWeightClick(event.hubId, event.hubName, event.currentValue)
			is HiveEvent.NavigateToHiveEdit           -> onHiveEditClick(event.hiveName)

			is HiveEvent.ShowSnackBar                 -> {
				snackbarHostState.showSnackbar(
					message = event.message,
					duration = SnackbarDuration.Short
				)
			}
		}
	}

	PullToRefreshBox(
		isRefreshing = isRefreshing,
		onRefresh = hiveViewModel::refresh
	) {
		when (val state = hiveUiState) {
			is HiveUiState.Loading -> FullScreenProgressIndicator()

			is HiveUiState.Error   -> ErrorMessage(
				message = state.message,
				onRetry = hiveViewModel::resetError
			)

			is HiveUiState.Content -> {
				val actions = HiveActions(
					onQueenClick = hiveViewModel::onQueenClick,
					onWorkClick = hiveViewModel::onWorksClick,
					onNotificationClick = hiveViewModel::onNotificationsClick,
					onTemperatureClick = hiveViewModel::onTemperatureClick,
					onNoiseClick = hiveViewModel::onNoiseClick,
					onWeightClick = hiveViewModel::onWeightClick,
					onHiveListClick = hiveViewModel::onHiveListClick,
					onHiveEditClick = hiveViewModel::onHiveEditClick,
					onDeleteClick = hiveViewModel::onDeleteClick
				)
				HiveContent(
					hive = state.hive,
					snackbarHostState = snackbarHostState,
					actions = actions,
					onWorkClick = hiveViewModel::onWorkClick,
					onBackClick = onHiveListClick
				)
			}
		}
	}
}

@Composable
private fun HiveContent(
	hive: HiveUi,
	snackbarHostState: SnackbarHostState,
	actions: HiveActions,
	onWorkClick: (String) -> Unit,
	onBackClick: () -> Unit
) {
	Scaffold(
		topBar = {
			AppTopBar(
				title = stringResource(R.string.hive),
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
						value = hive.name,
						modifier = Modifier.weight(0.6f)
					)
					InfoCard(
						title = stringResource(R.string.label_connected_hub),
						value = hive.hub?.name ?: stringResource(R.string.no),
						modifier = Modifier.weight(1f)
					)
				}
			}

			if (hive.hub != null) {
				Column(
					modifier = Modifier.fillMaxWidth(),
					verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
				) {
					SectionTitle(title = stringResource(R.string.section_latest_readings))
					Row(
						horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal),
						modifier = Modifier.fillMaxWidth()
					) {
						val temp = hive.hub.sensorReadings?.temperatureSensor
							?.let { stringResource(R.string.sensor_temperature_format, it.temperature.toFloat()) }
							?: stringResource(R.string.no)

						val noise = hive.hub.sensorReadings?.noiseSensor
							?.let { stringResource(R.string.sensor_noise_format, it.noise.toFloat()) }
							?: stringResource(R.string.no)

						val weight = hive.hub.sensorReadings?.weightSensor
							?.let { stringResource(R.string.sensor_weight_format, it.weight.toFloat()) }
							?: stringResource(R.string.no)

						InfoCard(
							title = stringResource(R.string.label_temperature),
							value = temp,
							modifier = Modifier
								.weight(1.5f)
								.clickable { actions.onTemperatureClick() }
						)
						InfoCard(
							title = stringResource(R.string.label_noise),
							value = noise,
							modifier = Modifier
								.weight(1f)
								.clickable { actions.onNoiseClick() }
						)
						InfoCard(
							title = stringResource(R.string.label_weight),
							value = weight,
							modifier = Modifier
								.weight(1f)
								.clickable { actions.onWeightClick() }
						)
					}
				}
			}

			if (hive.queen != null) {
				Column(
					modifier = Modifier.fillMaxWidth(),
					verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
				) {
					SectionTitle(title = stringResource(R.string.queen))
					QueenCard(
						queen = hive.queen,
						onClick = actions.onQueenClick,
						displayMode = QueenCardDisplayMode.Compact
					)
				}
			}

			Column(
				modifier = Modifier.fillMaxWidth(),
				verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
			) {
				SectionHeaderWithAction(
					title = stringResource(R.string.works_for_hive),
					actionText = stringResource(R.string.see_all),
					onActionClick = actions.onWorkClick
				)
				Row(
					horizontalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal),
					modifier = Modifier.fillMaxWidth()
				) {
					hive.recentWorks.forEach { work ->
						RecentWorkCard(
							work = work,
							onClick = { onWorkClick(work.id) },
							modifier = Modifier.weight(1f)
						)
					}
				}
			}

			Spacer(modifier = Modifier.height(Dimens.ItemsSpacingLarge))

			PrimaryButton(
				text = stringResource(R.string.edit),
				onClick = actions.onHiveEditClick,
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}

@Composable
private fun RecentWorkCard(work: WorkUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
	WorkTileCard(
		title = work.title,
		dateTime = work.dateTime,
		onClick = onClick,
		modifier = modifier
	)
}
