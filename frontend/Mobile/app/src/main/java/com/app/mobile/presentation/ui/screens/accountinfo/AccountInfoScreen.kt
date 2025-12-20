package com.app.mobile.presentation.ui.screens.accountinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.app.mobile.presentation.models.account.UserInfoModel
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.Title
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
            AccountInfoContent(currentState.userInfo, actions)
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
private fun AccountInfoContent(userInfo: UserInfoModel, actions: AccountInfoActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.ScreenContentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Title("Информация о пользователе")

        NameText(userInfo.name, actions.onNameClick)

        EmailText(userInfo.email, actions.onEmailClick)

        PasswordText(userInfo.password, actions.onPasswordClick)

        DeleteButton(actions.onDeleteClick)
    }
}

@Composable
private fun NameText(name: String, onNameClick: () -> Unit) {
    Text(
        name,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .padding(bottom = Dimens.ItemsSpacingMedium)
            .clickable(onClick = onNameClick)
    )
}

@Composable
private fun EmailText(email: String, onEmailClick: () -> Unit) {
    Text(
        email,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .padding(bottom = Dimens.ItemsSpacingMedium)
            .clickable(onClick = onEmailClick)
    )
}

@Composable
private fun PasswordText(password: String, onPasswordClick: () -> Unit) {
    Text(
        password,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .padding(bottom = Dimens.ItemsSpacingMedium)
            .clickable(onClick = onPasswordClick)
    )
}

@Composable
private fun DeleteButton(onDeleteClick: () -> Unit) {
    Button(
        modifier = Modifier,
        onClick = onDeleteClick
    ) {
        Text(text = "Удалить аккаунт")
    }
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
        AccountInfoContent(userInfo, actions)
    }
}
