package com.app.mobile.presentation.ui.screens.confirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.mobile.R
import com.app.mobile.presentation.models.TypeConfirmationUi
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.LabelButton
import com.app.mobile.presentation.ui.components.OtpTextField
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.screens.confirmation.models.ConfirmationActions
import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationFormState
import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationNavigationEvent
import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationUiState
import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationViewModel
import com.app.mobile.ui.theme.Dimens

@Composable
fun ConfirmationScreen(
    confirmationViewModel: ConfirmationViewModel,
    email: String,
    type: TypeConfirmationUi,
    onConfirmClick: () -> Unit
) {
    val confirmationUiState = confirmationViewModel.confirmationUiState.observeAsState(
        ConfirmationUiState.Loading
    )

    LaunchedEffect(key1 = Unit) {
        confirmationViewModel.createConfirmationModelUi(email, type)
    }

    val navigationEvent by confirmationViewModel.navigationEvent.observeAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { event ->
            when (event) {
                is ConfirmationNavigationEvent.NavigateToAuthorization -> {
                    onConfirmClick()
                    confirmationViewModel.onNavigationHandled()
                }
            }
        }
    }

    when (val state = confirmationUiState.value) {
        is ConfirmationUiState.Loading -> FullScreenProgressIndicator()
        is ConfirmationUiState.Error -> ErrorMessage(message = state.message) {}
        is ConfirmationUiState.Content -> {
            // Подписываемся на formState для отображения и валидации
            val formState = state.formState

            val actions = ConfirmationActions(
                onCodeChange = confirmationViewModel::onCodeChange,
                onConfirmClick = confirmationViewModel::onConfirmClick,
                onResendCodeClick = confirmationViewModel::onResendCode
            )
            ConfirmationContent(
                formState = formState,
                actions = actions
            )
        }
    }
}

@Composable
private fun ConfirmationContent(
    formState: ConfirmationFormState,
    actions: ConfirmationActions
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = Dimens.OpenScreensPaddingHorizontal,
                vertical = Dimens.OpenScreensPaddingVertical
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Title(
            text = stringResource(R.string.confirm_registration_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = Dimens.TitleTopPadding)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.enter_code),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            OtpTextField(
                value = formState.code,
                onValueChange = actions.onCodeChange,
                isError = formState.codeError != null
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LabelButton(
                    text = stringResource(R.string.resend_code),
                    onClick = { actions.onResendCodeClick() }
                )

                Text(
                    /* TODO: real timer */
                    text = "0:20",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        CodeConfirmButton(onClick = actions.onConfirmClick)
    }
}


@Composable
private fun CodeConfirmButton(onClick: () -> Unit) {
    PrimaryButton(
        text = stringResource(R.string.confirm),
        onClick = onClick,
        modifier = Modifier.padding(
            horizontal = 76.dp,
            vertical = Dimens.ButtonSoloVerticalPadding
        )
    )
}
