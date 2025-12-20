package com.app.mobile.presentation.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.screens.settings.models.SettingsActions
import com.app.mobile.presentation.ui.screens.settings.viewmodel.SettingsNavigationEvent
import com.app.mobile.presentation.ui.screens.settings.viewmodel.SettingsUiState
import com.app.mobile.presentation.ui.screens.settings.viewmodel.SettingsViewModel
import com.app.mobile.ui.theme.Dimens
import com.app.mobile.ui.theme.MobileTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onAccountInfoClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onAboutAppClick: () -> Unit
) {
    val settingsUiState by settingsViewModel.settingsUiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // что-то для резюма
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    LaunchedEffect(settingsViewModel.navigationEvent) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            settingsViewModel.navigationEvent.collectLatest { event ->
                when (event) {
                    is SettingsNavigationEvent.NavigateToAccountInfo -> onAccountInfoClick()

                    is SettingsNavigationEvent.NavigateToAboutApp -> onAboutAppClick()

                    is SettingsNavigationEvent.NavigateToAuthorization -> onLogoutClick()
                }
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
            SettingsContent(actions)
        }

        is SettingsUiState.Loading -> FullScreenProgressIndicator()

        is SettingsUiState.Error -> ErrorMessage(state.message, {})
    }
}

@Composable
private fun SettingsContent(actions: SettingsActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.ScreenContentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center

    ) {
        Title("Настройки")

        AccountInfoButton(actions.onAccountInfoClick)

        AboutAppButton(actions.onAboutAppClick)

        LogoutButton(actions.onLogoutClick)
    }
}

@Composable
private fun AccountInfoButton(onAccountInfoClick: () -> Unit) {
    Button(
        modifier = Modifier.padding(bottom = Dimens.ItemsSpacingMedium),
        onClick = onAccountInfoClick
    ) {
        Text(text = "Учетная запись")
    }
}

@Composable
private fun AboutAppButton(onAboutAppClick: () -> Unit) {
    Button(
        modifier = Modifier.padding(bottom = Dimens.ItemsSpacingMedium),
        onClick = onAboutAppClick
    ) {
        Text(text = " О приложении")
    }
}

@Composable
private fun LogoutButton(onLogoutClick: () -> Unit) {
    Button(
        modifier = Modifier,
        onClick = onLogoutClick
    ) {
        Text(text = "Выйти")
    }
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
        SettingsContent(actions)
    }
}
