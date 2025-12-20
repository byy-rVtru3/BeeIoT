package com.app.mobile.presentation.ui.screens.authorization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.LabelButton
import com.app.mobile.presentation.ui.components.PasswordTextField
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.components.ValidatedTextField
import com.app.mobile.presentation.ui.screens.authorization.models.AuthorizationActions
import com.app.mobile.presentation.ui.screens.authorization.viewmodel.AuthorizationFormState
import com.app.mobile.presentation.ui.screens.authorization.viewmodel.AuthorizationNavigationEvent
import com.app.mobile.presentation.ui.screens.authorization.viewmodel.AuthorizationUiState
import com.app.mobile.presentation.ui.screens.authorization.viewmodel.AuthorizationViewModel
import com.app.mobile.presentation.ui.screens.works.editor.viewmodel.WorksEditorNavigationEvent
import com.app.mobile.presentation.validators.ValidationConfig
import com.app.mobile.presentation.validators.ValidationError
import com.app.mobile.ui.theme.Dimens
import com.app.mobile.ui.theme.MobileTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AuthorizationScreen(
    authorizationViewModel: AuthorizationViewModel,
    onAuthorizeClick: () -> Unit,
    onRegistrationClick: () -> Unit
) {
    val authorizationUiState by authorizationViewModel.authorizationUiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                authorizationViewModel.createAuthorizationModel()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(authorizationViewModel.navigationEvent) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            authorizationViewModel.navigationEvent.collectLatest { event ->
                when (event) {
                    is AuthorizationNavigationEvent.NavigateToMainScreen -> onAuthorizeClick()
                    is AuthorizationNavigationEvent.NavigateToRegistration -> onRegistrationClick()
                }
            }
        }
    }

    val isValidationEnabled = remember { mutableStateOf(ValidationConfig.isValidationEnabled) }

    when (val state = authorizationUiState) {
        is AuthorizationUiState.Loading -> {
            FullScreenProgressIndicator()
        }

        is AuthorizationUiState.Error -> {
            ErrorMessage(message = state.message) {}
        }

        is AuthorizationUiState.Content -> {
            val formState = state.formState

            val actions = AuthorizationActions(
                onEmailChange = authorizationViewModel::onEmailChange,
                onPasswordChange = authorizationViewModel::onPasswordChange,
                onAuthorizeClick = authorizationViewModel::onAuthorizeClick,
                onRegistrationClick = authorizationViewModel::onRegistrationClick
            )

            Box(modifier = Modifier.fillMaxSize()) {
                AuthorizationContent(
                    formState = formState,
                    actions = actions
                )

                DeveloperPanel(
                    isValidationEnabled = isValidationEnabled,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(Dimens.ScreenContentPadding)
                )
            }
        }
    }
}

@Composable
private fun AuthorizationContent(
    formState: AuthorizationFormState,
    actions: AuthorizationActions
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
            text = stringResource(R.string.authorization_title),
            modifier = Modifier.padding(top = Dimens.TitleTopPadding)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingSmall)
        ) {
            AuthorizationEmailTextField(
                email = formState.email,
                emailError = formState.emailError,
                onEmailChange = actions.onEmailChange
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                AuthorizationPasswordTextField(
                    password = formState.password,
                    passwordError = formState.passwordError,
                    onPasswordChange = actions.onPasswordChange
                )

                ForgotPasswordButton(onClick = { /* TODO */ })
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingMedium),
            modifier = Modifier
                .padding(
                    horizontal = Dimens.ButtonHorizontalPadding
                )
                .padding(bottom = Dimens.ButtonTwiceVerticalPadding)
        ) {
            AuthorizationButton(onClick = actions.onAuthorizeClick)
            RegistrationButton(onClick = actions.onRegistrationClick)
        }
    }
}

@Composable
fun AuthorizationEmailTextField(
    email: String,
    emailError: ValidationError?,
    onEmailChange: (String) -> Unit
) {
    ValidatedTextField(
        value = email,
        onValueChange = onEmailChange,
        placeholder = stringResource(R.string.email),
        error = emailError
    )
}

@Composable
fun AuthorizationPasswordTextField(
    password: String,
    passwordError: ValidationError?,
    onPasswordChange: (String) -> Unit
) {
    PasswordTextField(
        value = password,
        onValueChange = onPasswordChange,
        placeholder = stringResource(R.string.password),
        error = passwordError
    )
}

@Composable
fun AuthorizationButton(onClick: () -> Unit) {
    PrimaryButton(
        text = stringResource(R.string.authorization_button),
        onClick = onClick
    )
}

@Composable
fun RegistrationButton(onClick: () -> Unit) {
    PrimaryButton(
        text = stringResource(R.string.registration_button),
        onClick = onClick
    )
}

@Composable
fun ForgotPasswordButton(onClick: () -> Unit) {
    LabelButton(
        text = stringResource(R.string.forgot_password),
        onClick = onClick,
        modifier = Modifier.padding(top = Dimens.TextFieldErrorTopPadding)
    )
}

@Preview(showBackground = true)
@Composable
fun AuthorizationContentPreview() {
    MobileTheme {
        val formState = AuthorizationFormState()
        val actions = AuthorizationActions(
            onEmailChange = {},
            onPasswordChange = {},
            onAuthorizeClick = {},
            onRegistrationClick = {}
        )
        AuthorizationContent(
            formState = formState,
            actions = actions
        )
    }
}