package com.app.mobile.presentation.ui.screens.aboutapp

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.InfoScreenContent
import com.app.mobile.presentation.ui.components.InfoSection
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.screens.aboutapp.viewmodel.AboutAppEvent
import com.app.mobile.presentation.ui.screens.aboutapp.viewmodel.AboutAppUiState
import com.app.mobile.presentation.ui.screens.aboutapp.viewmodel.AboutAppViewModel
import com.app.mobile.ui.theme.MobileTheme

@Composable
fun AboutAppScreen(aboutAppViewModel: AboutAppViewModel, onBackClick: () -> Unit) {

	val aboutAppUiState by aboutAppViewModel.uiState.collectAsStateWithLifecycle()
	val snackbarHostState = remember { SnackbarHostState() }

	ObserveAsEvents(aboutAppViewModel.event) { event ->
		when (event) {
			is AboutAppEvent.NavigateBack -> onBackClick()

			is AboutAppEvent.ShowSnackBar -> {
				snackbarHostState.showSnackbar(
					event.message,
					duration = SnackbarDuration.Short
				)
			}
		}
	}

	when (val state = aboutAppUiState) {
		is AboutAppUiState.Content -> AboutAppContent(onBackClick, snackbarHostState)
		is AboutAppUiState.Error   -> ErrorMessage(state.message, {})
		is AboutAppUiState.Loading -> FullScreenProgressIndicator()

	}
}

@Composable
private fun AboutAppContent(onBackClick: () -> Unit, snackbarHostState: SnackbarHostState) {
	InfoScreenContent(
		title = stringResource(R.string.about),
		sections = listOf(
			InfoSection(body = stringResource(R.string.app_info))
		),
		onBackClick = onBackClick,
		snackbarHostState = snackbarHostState
	)
}

@Preview(showBackground = true)
@Composable
fun AboutAppContentPreview() {
	MobileTheme {
		AboutAppContent(onBackClick = {}, snackbarHostState = remember { SnackbarHostState() })
	}
}
