package com.app.mobile.presentation.ui.screens.hive.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.app.mobile.presentation.ui.components.AppTopBar
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.InfoCard
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.SectionTitle
import com.app.mobile.presentation.ui.components.TopBarAction
import com.app.mobile.presentation.ui.screens.hive.details.models.HiveActions
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveEvent
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveUiState
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveViewModel
import com.app.mobile.ui.theme.Dimens

@Composable
fun HiveScreen(
	hiveViewModel: HiveViewModel,
	onQueenClick: (queenName: String) -> Unit,
	onWorksClick: (hiveName: String) -> Unit,
	onNotificationsClick: (hiveName: String) -> Unit,
	onTemperatureClick: (hiveName: String) -> Unit,
	onNoiseClick: (hiveName: String) -> Unit,
	onWeightClick: (hiveName: String) -> Unit,
	onHiveListClick: () -> Unit,
	onHiveEditClick: (hiveName: String) -> Unit
) {
	val hiveUiState by hiveViewModel.uiState.collectAsStateWithLifecycle()
	val snackbarHostState = remember { SnackbarHostState() }

	LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
		hiveViewModel.loadHive()
	}

	ObserveAsEvents(hiveViewModel.event) { event ->
		when (event) {
			is HiveEvent.NavigateToHiveList           -> onHiveListClick()
			is HiveEvent.NavigateToQueenByHive        -> onQueenClick(event.queenName)
			is HiveEvent.NavigateToWorkByHive         -> onWorksClick(event.hiveName)
			is HiveEvent.NavigateToNotificationByHive -> onNotificationsClick(event.hiveName)
			is HiveEvent.NavigateToTemperatureByHive  -> onTemperatureClick(event.hiveName)
			is HiveEvent.NavigateToNoiseByHive        -> onNoiseClick(event.hiveName)
			is HiveEvent.NavigateToWeightByHive       -> onWeightClick(event.hiveName)
			is HiveEvent.NavigateToHiveEdit           -> onHiveEditClick(event.hiveName)

			is HiveEvent.ShowSnackBar                 -> {
				snackbarHostState.showSnackbar(
					message = event.message,
					duration = SnackbarDuration.Short
				)
			}
		}
	}

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
				onDeleteClick = {}
			)
			HiveContent(state.hive, snackbarHostState, actions, onBackClick = onHiveListClick)
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HiveContent(
	hive: HiveUi,
	snackbarHostState: SnackbarHostState,
	actions: HiveActions,
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
						modifier = Modifier.weight(0.6f).fillMaxWidth(0.48f)
					)

					InfoCard(
						title = stringResource(R.string.label_connected_hub),
						value = hive.hubName ?: stringResource(R.string.no),
						modifier = Modifier.weight(1f).fillMaxWidth(0.48f)
					)
				}
			}

			if (hive.queenName != null) {
				Column(
					modifier = Modifier.fillMaxWidth(),
					verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
				) {
					SectionTitle(title = stringResource(R.string.queen))
					InfoCard(
						title = stringResource(R.string.queen),
						value = hive.queenName
					)
				}
			}

			Spacer(modifier = Modifier.height(Dimens.ItemsSpacingLarge))
		}
	}
}
