package com.app.mobile.presentation.ui.screens.hive.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.presentation.models.hive.HiveUi
import com.app.mobile.presentation.models.hive.HubUi
import com.app.mobile.presentation.models.hive.QueenUi
import com.app.mobile.presentation.ui.components.AppTopBar
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.TopBarAction
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.screens.hive.details.models.HiveActions
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveNavigationEvent
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveUiState
import com.app.mobile.presentation.ui.screens.hive.details.viewmodel.HiveViewModel
import com.app.mobile.ui.theme.Dimens
import kotlinx.coroutines.flow.collectLatest
import com.app.mobile.R
import com.app.mobile.presentation.ui.components.DetailsItemCard
import com.app.mobile.presentation.ui.components.InfoCard
import com.app.mobile.presentation.ui.components.QueenCard
import com.app.mobile.presentation.ui.components.SectionHeaderWithAction
import com.app.mobile.presentation.ui.components.SectionTitle

@Composable
fun HiveScreen(
    hiveViewModel: HiveViewModel,
    onQueenClick: (queenId: String) -> Unit,
    onWorksClick: (hiveId: String) -> Unit,
    onNotificationsClick: (hiveId: String) -> Unit,
    onTemperatureClick: (hiveId: String) -> Unit,
    onNoiseClick: (hiveId: String) -> Unit,
    onWeightClick: (hiveId: String) -> Unit,
    onHiveListClick: () -> Unit,
    onHiveEditClick: (hiveId: String) -> Unit
) {
    val hiveUiState by hiveViewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hiveViewModel.loadHive()
    }

    ObserveAsEvents(hiveViewModel.event) { event ->
        when (event) {
            is HiveNavigationEvent.NavigateToHiveList -> onHiveListClick()
            is HiveNavigationEvent.NavigateToQueenByHive -> onQueenClick(event.queenId)
            is HiveNavigationEvent.NavigateToWorkByHive -> onWorksClick(event.hiveId)
            is HiveNavigationEvent.NavigateToNotificationByHive -> onNotificationsClick(
                event.hiveId
            )

            is HiveNavigationEvent.NavigateToTemperatureByHive -> onTemperatureClick(event.hiveId)
            is HiveNavigationEvent.NavigateToNoiseByHive -> onNoiseClick(event.hiveId)
            is HiveNavigationEvent.NavigateToWeightByHive -> onWeightClick(event.hiveId)
            is HiveNavigationEvent.NavigateToHiveEdit -> onHiveEditClick(event.hiveId)
        }
    }

    when (val state = hiveUiState) {
        is HiveUiState.Loading -> FullScreenProgressIndicator()

        is HiveUiState.Error -> ErrorMessage(
            message = state.message,
            onRetry = {}
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
                onDeleteClick = {} // необходимо добавить удаление, сейчас мне лень
            )
            HiveContent(state.hive, actions, onBackClick = onHiveListClick)
        }
    }
}

@Composable
private fun HiveContent(
    hive: HiveUi,
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
                        modifier = Modifier.weight(0.8f)
                    )

                    val hubName = if (hive.connectedHub is HubUi.Present) {
                        hive.connectedHub.name
                    } else {
                        stringResource(R.string.no)
                    }
                    InfoCard(
                        title = stringResource(R.string.label_connected_hub),
                        value = hubName,
                        modifier = Modifier.weight(1.2f)
                    )
                }

            }

            if (hive.queen is QueenUi.Present) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
                ) {
                    SectionTitle(title = stringResource(R.string.queen))
                    QueenCard(queen = hive.queen, onClick = actions.onQueenClick)
                }
            }

//            Column(
//                modifier = Modifier.fillMaxWidth(),
//                verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
//            ) {
//                SectionHeaderWithAction(
//                    title = stringResource(R.string.notifications),
//                    actionText = stringResource(R.string.see_all),
//                    onActionClick = actions.onNotificationClick
//                )
//
//                if (hive.notifications.isNotEmpty()) {
//                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)) {
//                        hive.notifications.take(2).forEach { notification ->
//                            DetailsItemCard(
//                                title = stringResource(R.string.notification), // Заглушка, пока модель не обновится
//                                description = notification.message,
//                                footer = notification.dateTime
//                            )
//                        }
//                    }
//                }
//            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
            ) {
                SectionHeaderWithAction(
                    title = stringResource(R.string.works_for_hive),
                    actionText = stringResource(R.string.see_all),
                    onActionClick = actions.onWorkClick
                )

                if (hive.works.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)) {
                        hive.works.take(2).forEach { work ->
                            DetailsItemCard(
                                title = work.title,
                                description = work.text,
                                footer = work.dateTime
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.ItemsSpacingLarge))
        }
    }
}