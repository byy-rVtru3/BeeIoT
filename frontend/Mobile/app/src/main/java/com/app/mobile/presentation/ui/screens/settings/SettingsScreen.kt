package com.app.mobile.presentation.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.LogoCircle
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.SettingsButton
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.screens.settings.models.SettingsActions
import com.app.mobile.presentation.ui.screens.settings.viewmodel.SettingsEvent
import com.app.mobile.presentation.ui.screens.settings.viewmodel.SettingsUiState
import com.app.mobile.presentation.ui.screens.settings.viewmodel.SettingsViewModel
import com.app.mobile.ui.theme.Dimens
import com.app.mobile.ui.theme.MobileTheme

@Composable
fun SettingsScreen(
	settingsViewModel: SettingsViewModel,
	onAccountInfoClick: () -> Unit,
	onLogoutClick: () -> Unit,
	onAboutAppClick: () -> Unit
) {
	val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
	val snackBarHostState = remember { SnackbarHostState() }

	ObserveAsEvents(settingsViewModel.event) { event ->
		when (event) {
			is SettingsEvent.NavigateToAccountInfo -> onAccountInfoClick()

			is SettingsEvent.NavigateToAboutApp -> onAboutAppClick()

			is SettingsEvent.NavigateToAuthorization -> onLogoutClick()

			is SettingsEvent.ShowSnackBar -> {
				snackBarHostState.showSnackbar(
					message = event.message,
					duration = SnackbarDuration.Short
				)
			}
		}
	}

	when (val state = settingsUiState) {
		is SettingsUiState.Content -> {
			val actions = SettingsActions(
				onAccountInfoClick = settingsViewModel::onAccountInfoClick,
				onAboutAppClick = settingsViewModel::onAboutAppClick,
				onLogoutClick = settingsViewModel::onLogoutClick
			)
			SettingsContent(actions, snackBarHostState)
		}

		is SettingsUiState.Loading -> FullScreenProgressIndicator()

		is SettingsUiState.Error   -> ErrorMessage(state.message, settingsViewModel::resetError)
	}
}

@Composable
private fun SettingsContent(actions: SettingsActions, snackBarHostState: SnackbarHostState) {
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
	) { paddingValues ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues)
				.padding(Dimens.ScreenContentPadding),
			horizontalAlignment = Alignment.CenterHorizontally,

			) {
			Column(
				modifier = Modifier.padding(top = Dimens.TitleTopPadding),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingSmallMedium)
			) {
				LogoCircle()
				Title(
					text = stringResource(R.string.settings),
					modifier = Modifier.padding(

						bottom = Dimens.SettingsTitleBottomPadding
					),
					style = MaterialTheme.typography.headlineSmall
				)
			}

			Column(
				verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingMedium),
				modifier = Modifier
					.fillMaxSize()

			) {
				AccountInfoButton(actions.onAccountInfoClick)

				AboutAppButton(actions.onAboutAppClick)

				LogoutButton(actions.onLogoutClick)
			}

		}
	}
}

@Composable
private fun AccountInfoButton(onAccountInfoClick: () -> Unit) {
	SettingsButton(
		onClick = onAccountInfoClick,
		text = stringResource(R.string.account),

		)
}

@Composable
private fun AboutAppButton(onAboutAppClick: () -> Unit) {
	SettingsButton(
		onClick = onAboutAppClick,
		text = stringResource(R.string.about),

		)
}

@Composable
private fun LogoutButton(onLogoutClick: () -> Unit) {
	SettingsButton(
		onClick = onLogoutClick,
		text = stringResource(R.string.logout),
		exit = true,
	)
}

@Preview(showBackground = true)
@Composable
fun SettingsContentPreview() {
	MobileTheme {
		val actions = SettingsActions(
			onAccountInfoClick = {},
			onAboutAppClick = {},
			onLogoutClick = {}
		)
		SettingsContent(
			actions,
			snackBarHostState = SnackbarHostState()
		)
	}
}
