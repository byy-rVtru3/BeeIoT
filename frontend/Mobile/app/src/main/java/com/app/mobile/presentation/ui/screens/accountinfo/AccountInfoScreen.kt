package com.app.mobile.presentation.ui.screens.accountinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.mobile.presentation.models.UserInfoModel
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.screens.accountinfo.viewmodel.AccountInfoDialogState
import com.app.mobile.presentation.ui.screens.accountinfo.viewmodel.AccountInfoUiState
import com.app.mobile.presentation.ui.screens.accountinfo.viewmodel.AccountInfoViewModel

@Composable
fun AccountInfoScreen(accountInfoViewModel: AccountInfoViewModel) {

    val accountInfoUiState by accountInfoViewModel.accountInfoUiState.observeAsState(
        AccountInfoUiState.Loading
    )

    val accountInfoDialogState by accountInfoViewModel.accountInfoDialogState.observeAsState(
        AccountInfoDialogState.Hidden
    )

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

    LaunchedEffect(key1 = Unit) {
        accountInfoViewModel.getAccountInfo()
    }

    when (val currentState = accountInfoUiState) {
        is AccountInfoUiState.Loading -> FullScreenProgressIndicator()
        is AccountInfoUiState.Error -> ErrorMessage(currentState.message, {})
        is AccountInfoUiState.Content -> AccountInfoContent(currentState.userInfo)
    }
}

@Composable
private fun AccountInfoContent(userInfo: UserInfoModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Title("Информация о пользователе")

        NameText(userInfo.name)

        EmailText(userInfo.email)

        PasswordText(userInfo.password)
    }
}

@Composable
private fun NameText(name: String) {
    Text(name, modifier = Modifier.padding(bottom = 16.dp))
}

@Composable
private fun EmailText(email: String) {
    Text(email, modifier = Modifier.padding(bottom = 16.dp))
}

@Composable
private fun PasswordText(password: String) {
    Text(password)
}

@Preview(showBackground = true)
@Composable
fun AccountInfoScreenPreview() {
    AccountInfoContent(UserInfoModel("Name", "Email", "Password"))
}