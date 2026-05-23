package com.app.mobile.presentation.ui.screens.hub.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.hub.HubModel
import com.app.mobile.presentation.ui.components.AppTopBar
import com.app.mobile.presentation.ui.components.CustomTextField
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.screens.hub.editor.models.HubEditorActions
import com.app.mobile.presentation.ui.screens.hub.editor.qr.rememberHubQrScannerLauncher
import com.app.mobile.presentation.ui.screens.hub.editor.viewmodel.HubEditorEvent
import com.app.mobile.presentation.ui.screens.hub.editor.viewmodel.HubEditorUiState
import com.app.mobile.presentation.ui.screens.hub.editor.viewmodel.HubEditorViewModel
import com.app.mobile.ui.theme.Dimens

@Composable
fun HubEditorScreen(
	hubEditorViewModel: HubEditorViewModel,
	onBackClick: () -> Unit
) {
	val hubEditorUiState by hubEditorViewModel.uiState.collectAsStateWithLifecycle()
	val snackbarHostState = remember { SnackbarHostState() }
	val scannerLauncher = rememberHubQrScannerLauncher()
	val invalidQrMessage = stringResource(R.string.hub_qr_invalid)
	val scannerUnavailableMessage = stringResource(R.string.hub_qr_scanner_unavailable)

	LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
		hubEditorViewModel.loadHub()
	}

	ObserveAsEvents(hubEditorViewModel.event) { event ->
		val message = when (event) {
			is HubEditorEvent.NavigateBack         -> {
				onBackClick(); return@ObserveAsEvents
			}

			is HubEditorEvent.ShowSnackBar         -> event.message
			is HubEditorEvent.QrScanInvalid        -> invalidQrMessage
			is HubEditorEvent.QrScannerUnavailable -> scannerUnavailableMessage
		}
		snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
	}

	when (val state = hubEditorUiState) {
		is HubEditorUiState.Loading -> FullScreenProgressIndicator()
		is HubEditorUiState.Error   -> ErrorMessage(state.message, onRetry = {})

		is HubEditorUiState.Content -> {
			val actions = HubEditorActions(
				onNameChange = hubEditorViewModel::onNameChange,
				onIdChange = hubEditorViewModel::onIdChange,
				onScanQrClick = {
					scannerLauncher.scan(
						onResult = hubEditorViewModel::onQrScanned,
						onError = hubEditorViewModel::onQrScanFailed
					)
				},
				onSaveClick = hubEditorViewModel::onSaveClick
			)
			HubEditorContent(
				hubModel = state.hubModel,
				snackbarHostState = snackbarHostState,
				actions = actions,
				onBackClick = onBackClick,
				isNew = hubEditorViewModel.isNew
			)
		}
	}
}

@Composable
private fun HubEditorContent(
	hubModel: HubModel,
	isNew: Boolean,
	snackbarHostState: SnackbarHostState,
	actions: HubEditorActions,
	onBackClick: () -> Unit
) {
	val title = if (hubModel.name.isEmpty()) {
		stringResource(R.string.hub_add_title)
	} else {
		stringResource(R.string.hub_edit_title)
	}

	Scaffold(
		topBar = {
			AppTopBar(
				title = title,
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

			HubEditorField(
				label = stringResource(R.string.hub_name_label),
				value = hubModel.name,
				placeholder = stringResource(R.string.hub_name_placeholder),
				onValueChange = actions.onNameChange
			)

			Spacer(modifier = Modifier.height(Dimens.ItemSpacingNormal))

			if (isNew) {
				HubEditorField(
					label = stringResource(R.string.hub_id_label),
					value = hubModel.id,
					placeholder = stringResource(R.string.hub_id_placeholder),
					onValueChange = actions.onIdChange,
					trailingIcon = {
						Icon(
							imageVector = Icons.Outlined.QrCodeScanner,
							contentDescription = stringResource(R.string.hub_id_scan_qr),
							tint = MaterialTheme.colorScheme.primary,
							modifier = Modifier
								.size(Dimens.TextFieldIconSize)
								.clickable(onClick = actions.onScanQrClick)
						)
					}
				)

				Spacer(modifier = Modifier.weight(1f))
			}

			PrimaryButton(
				text = stringResource(R.string.save),
				onClick = actions.onSaveClick,
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}

@Composable
private fun HubEditorField(
	label: String,
	value: String,
	placeholder: String,
	onValueChange: (String) -> Unit,
	modifier: Modifier = Modifier,
	trailingIcon: @Composable (() -> Unit)? = null
) {
	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(Dimens.ItemSpacingNormal)
	) {
		Text(
			text = label,
			style = MaterialTheme.typography.labelLarge,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
		CustomTextField(
			value = value,
			onValueChange = onValueChange,
			placeholder = placeholder,
			modifier = Modifier.fillMaxWidth(),
			trailingIcon = trailingIcon
		)
	}
}
