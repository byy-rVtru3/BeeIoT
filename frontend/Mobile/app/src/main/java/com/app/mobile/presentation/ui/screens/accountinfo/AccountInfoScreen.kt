package com.app.mobile.presentation.ui.screens.accountinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.account.UserInfoModel
import com.app.mobile.presentation.ui.components.AppTopBar
import com.app.mobile.presentation.ui.components.ClickableProfileField
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.TopBarAction
import com.app.mobile.presentation.ui.screens.accountinfo.models.AccountInfoActions
import com.app.mobile.presentation.ui.screens.accountinfo.viewmodel.AccountInfoDialogState
import com.app.mobile.presentation.ui.screens.accountinfo.viewmodel.AccountInfoNavigationEvent
import com.app.mobile.presentation.ui.screens.accountinfo.viewmodel.AccountInfoUiState
import com.app.mobile.presentation.ui.screens.accountinfo.viewmodel.AccountInfoViewModel
import com.app.mobile.ui.theme.Dimens
import com.app.mobile.ui.theme.MobileTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AccountInfoScreen(
    accountInfoViewModel: AccountInfoViewModel,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit
) {

    val accountInfoUiState by accountInfoViewModel.accountInfoUiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accountInfoViewModel.getAccountInfo()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when (val currentState = accountInfoUiState) {
        is AccountInfoUiState.Loading -> FullScreenProgressIndicator()
        is AccountInfoUiState.Error -> ErrorMessage(currentState.message, {})
        is AccountInfoUiState.Content -> {
            val actions = AccountInfoActions(
                onNameClick = accountInfoViewModel::onNameClick,
                onEmailClick = accountInfoViewModel::onEmailClick,
                onPasswordClick = accountInfoViewModel::onPasswordClick,
                onDeleteClick = accountInfoViewModel::onDeleteAccountClick
            )
            AccountInfoContent(
                userInfo = currentState.userInfo,
                actions = actions,
                onBackClick = onBackClick
            )
        }
    }

    LaunchedEffect(accountInfoViewModel.navigationEvent) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            accountInfoViewModel.navigationEvent.collectLatest { event ->
                when (event) {
                    is AccountInfoNavigationEvent.NavigateToRegistration -> onDeleteClick()
                    is AccountInfoNavigationEvent.NavigateBack -> onBackClick()
                }
            }
        }
    }

    val accountInfoDialogState by accountInfoViewModel.accountInfoDialogState.collectAsStateWithLifecycle()

    when (val state = accountInfoDialogState) {
        is AccountInfoDialogState.SetName -> {
            //Name dialog
        }

        is AccountInfoDialogState.SetEmail -> {
            //Email dialog
        }

        is AccountInfoDialogState.SetPassword -> {
            //Password dialog
        }

        is AccountInfoDialogState.Hidden -> {
            //Hidden dialog
        }
    }
}

@Composable
private fun AccountInfoContent(userInfo: UserInfoModel, actions: AccountInfoActions,onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.account),
                onBackClick = onBackClick,
                hasBackground = false,
                action = TopBarAction.Delete(onClick = actions.onDeleteClick)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Важно: учитываем высоту TopBar
                .padding(Dimens.ScreenContentPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Dimens.AccountInfoTextFieldsHorizontalPadding
                    ),
                verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingMedium),
            ) {
                NameText(userInfo.name, actions.onNameClick)

                EmailText(userInfo.email, actions.onEmailClick)

                PasswordText(userInfo.password, actions.onPasswordClick)

                Text(
                    text = stringResource(R.string.hint_account_info),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }


        }
    }
}

@Composable
private fun NameText(name: String, onNameClick: () -> Unit) {
    ClickableProfileField(
        label = stringResource(R.string.name),
        value = name,
        onClick = onNameClick
    )
}

@Composable
private fun EmailText(email: String, onEmailClick: () -> Unit) {
    ClickableProfileField(
        label = stringResource(R.string.email),
        value = email,
        onClick = onEmailClick
    )
}

@Composable
private fun PasswordText(password: String, onPasswordClick: () -> Unit) {
    ClickableProfileField(
        label = stringResource(R.string.password),
        value = password,
        onClick = onPasswordClick
    )
}


@Preview(showBackground = true)
@Composable
fun AccountInfoContentPreview() {
    MobileTheme {
        val userInfo = UserInfoModel(
            name = "Иван Иванов",
            email = "ivan@example.com",
            password = "••••••••"
        )
        val actions = AccountInfoActions(
            onNameClick = {},
            onEmailClick = {},
            onPasswordClick = {},
            onDeleteClick = {}
        )
        AccountInfoContent(userInfo, actions, {})
    }
}
