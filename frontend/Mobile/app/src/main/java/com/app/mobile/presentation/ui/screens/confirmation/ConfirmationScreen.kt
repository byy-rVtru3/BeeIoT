package com.app.mobile.presentation.ui.screens.confirmation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.components.ValidatedTextField
import com.app.mobile.presentation.ui.screens.confirmation.models.ConfirmationActions
import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationFormState
import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationNavigationEvent
import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationUiState
import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationViewModel
import com.app.mobile.presentation.validators.ValidationError

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
            .padding(36.dp, 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Title(
            text = stringResource(R.string.confirm_registration_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 52.dp)
        )


        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.fillMaxWidth(),
        ) {
            CodeTextField(
                code = formState.code,
                codeError = formState.codeError,
                onCodeChange = actions.onCodeChange
            )
            CodeResendButton(onClick = actions.onResendCodeClick)
        }


        CodeConfirmButton(onClick = actions.onConfirmClick)
    }
}

@Composable
private fun CodeTextField(
    code: String,
    codeError: ValidationError?,
    onCodeChange: (String) -> Unit
) {
    ValidatedTextField(
        value = code,
        onValueChange = onCodeChange,
        placeholder = stringResource(R.string.enter_code),
        error = codeError
    )
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
        modifier = Modifier.padding(4.dp, 16.dp)
    )
}