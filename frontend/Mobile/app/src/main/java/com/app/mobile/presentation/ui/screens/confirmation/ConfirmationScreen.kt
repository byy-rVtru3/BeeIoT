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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.account.TypeConfirmationUi
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
import com.app.mobile.presentation.ui.screens.works.editor.viewmodel.WorksEditorNavigationEvent
import com.app.mobile.ui.theme.Dimens
import com.app.mobile.ui.theme.MobileTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ConfirmationScreen(
    confirmationViewModel: ConfirmationViewModel,
    onConfirmClick: () -> Unit
) {
    val confirmationUiState =
        confirmationViewModel.confirmationUiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                confirmationViewModel.createConfirmationModelUi()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(confirmationViewModel.navigationEvent) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            confirmationViewModel.navigationEvent.collectLatest { event ->
                when (event) {
                    is ConfirmationNavigationEvent.NavigateToAuthorization -> onConfirmClick()
                }
            }
        }
    }

    when (val state = confirmationUiState.value) {
        is ConfirmationUiState.Loading -> FullScreenProgressIndicator()
        is ConfirmationUiState.Error -> ErrorMessage(message = state.message) {}
        is ConfirmationUiState.Content -> {
            val formState = state.formState

            val actions = ConfirmationActions(
                onCodeChange = confirmationViewModel::onCodeChange,
                onConfirmClick = confirmationViewModel::onConfirmClick,
                onResendCodeClick = confirmationViewModel::onResendCode
            )
            ConfirmationContent(
                formState = formState,
                canResendCode = state.canResendCode,
                resendTimerSeconds = state.resendTimerSeconds,
                actions = actions
            )
        }
    }
}

@Composable
private fun ConfirmationContent(
    formState: ConfirmationFormState,
    canResendCode: Boolean,
    resendTimerSeconds: Int,
    actions: ConfirmationActions
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = Dimens.OpenScreenPaddingHorizontal,
                vertical = Dimens.OpenScreenPaddingVertical
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
                modifier = Modifier.padding(bottom = Dimens.ItemsSpacingLarge)
            )

            OtpTextField(
                value = formState.code,
                onValueChange = actions.onCodeChange,
                isError = formState.codeError != null
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.OtpCellSpacing),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LabelButton(
                    text = stringResource(R.string.resend_code),
                    onClick = { actions.onResendCodeClick() },
                    enabled = canResendCode
                )

                if (resendTimerSeconds > 0) {
                    val minutes = resendTimerSeconds / 60
                    val seconds = resendTimerSeconds % 60
                    Text(
                        text = "$minutes:${seconds.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
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
        modifier = Modifier
            .padding(
                horizontal = Dimens.ButtonHorizontalPaddingLarge
            )
            .padding(bottom = Dimens.ButtonSoloVerticalPadding)
    )
}

@Preview(showBackground = true)
@Composable
fun ConfirmationContentPreview() {
    MobileTheme {
        val formState = ConfirmationFormState()
        val actions = ConfirmationActions(
            onCodeChange = {},
            onConfirmClick = {},
            onResendCodeClick = {}
        )
        ConfirmationContent(
            formState = formState,
            canResendCode = true,
            resendTimerSeconds = 30,
            actions = actions
        )
    }
}
