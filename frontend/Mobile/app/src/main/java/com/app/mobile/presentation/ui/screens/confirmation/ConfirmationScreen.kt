package com.app.mobile.presentation.ui.screens.confirmation

import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.mobile.R
import com.app.mobile.presentation.models.ConfirmationModelUi
import com.app.mobile.presentation.ui.components.CustomTextField
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.LabelButton
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.screens.confirmation.models.ConfirmationActions
import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationUiState
import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationViewModel

@Composable
fun ConfirmationScreen(confirmationViewModel: ConfirmationViewModel, email: String, type: String) {

    LaunchedEffect(key1 = Unit) {
        confirmationViewModel.createConfirmationModelUi(email, type)
    }

    val confirmationUiState = confirmationViewModel.confirmationUiState.observeAsState(
        ConfirmationUiState.Loading)

    when (val state = confirmationUiState.value) {
        is ConfirmationUiState.Loading -> FullScreenProgressIndicator()
        is ConfirmationUiState.Error -> ErrorMessage(message = state.message, onRetry = {})
        is ConfirmationUiState.Content -> {
            val actions = ConfirmationActions(
                onCodeChange = confirmationViewModel::onCodeChange,
                onConfirmClick = confirmationViewModel::onConfirmClick,
                onResendCodeClick = confirmationViewModel::onResendCode
            )
            ConfirmationContent(
                confirmationModelUi = state.confirmationModelUi,
                actions = actions
            )
        }
    }
}

@Composable
private fun ConfirmationContent(confirmationModelUi: ConfirmationModelUi, actions:
ConfirmationActions) {

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(36.dp, 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Title(
            text = stringResource(R.string.confirm_registration_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 206.dp)
        )


        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxWidth().padding(bottom = 184.dp),) {
            CodeTextField(code = confirmationModelUi.code, onCodeChange = actions.onCodeChange)
            CodeResendButton(onClick = actions.onResendCodeClick)
        }


        CodeConfirmButton(onClick = actions.onConfirmClick)
    }
}

@Composable
private fun CodeTextField(code: String, onCodeChange: (String) -> Unit) {
    CustomTextField(code, onValueChange = onCodeChange, stringResource(R.string.enter_code))
}

@Composable
private fun CodeConfirmButton(onClick: () -> Unit) {
    PrimaryButton(
        text = stringResource(R.string.confirm),
        onClick = onClick,
        modifier = Modifier.padding(20.dp)
    )
}


@Composable
private fun CodeResendButton(onClick: () -> Unit) {
    LabelButton(
        text = stringResource(R.string.resend_code),
        onClick = onClick,
        modifier = Modifier.padding(4.dp,16.dp)
    )
}