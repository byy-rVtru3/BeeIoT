package com.app.mobile.presentation.ui.screens.authorization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.app.mobile.data.mock.MockDataSourceImpl
import com.app.mobile.presentation.validators.ValidationConfig
import com.app.mobile.presentation.validators.ValidationError
import com.app.mobile.ui.theme.Dimens
import org.koin.compose.koinInject

@Composable
fun AuthorizationScreen(
    authorizationViewModel: AuthorizationViewModel,
    onAuthorizeClick: () -> Unit,
    onRegistrationClick: () -> Unit
) {
    val authorizationUiState by authorizationViewModel.authorizationUiState.observeAsState(
        AuthorizationUiState.Loading
    )

    LaunchedEffect(key1 = Unit) {
        authorizationViewModel.createAuthorizationModel()
    }

    val navigationEvent by authorizationViewModel.navigationEvent.observeAsState()
    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { event ->
            when (event) {
                is AuthorizationNavigationEvent.NavigateToMainScreen -> {
                    onAuthorizeClick()
                    authorizationViewModel.onNavigationHandled()
                }

                is AuthorizationNavigationEvent.NavigateToRegistration -> {
                    onRegistrationClick()
                    authorizationViewModel.onNavigationHandled()
                }
            }
        }
    }

    // Получаем MockDataSource - он всегда доступен
    val mockDataSource: MockDataSourceImpl = koinInject()

    // Создаём state один раз
    val isMockEnabled = remember { mutableStateOf(false) }
    val isValidationEnabled = remember { mutableStateOf(false) }

    // Синхронизируем состояние при каждом появлении экрана
    LaunchedEffect(Unit) {
        isMockEnabled.value = mockDataSource.isMock()
        isValidationEnabled.value = mockDataSource.isValidationEnabled()
        ValidationConfig.init(mockDataSource)
    }

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
                    mockDataSource = mockDataSource,
                    isMockEnabled = isMockEnabled,
                    isValidationEnabled = isValidationEnabled,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
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
                horizontal = Dimens.OpenScreensPaddingHorizontal,
                vertical = Dimens.OpenScreensPaddingVertical
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
            verticalArrangement = Arrangement.spacedBy(Dimens.FieldsSpacing)
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical =Dimens.ButtonTwiceVerticalPadding, horizontal=48.dp)
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
        modifier = Modifier.padding(top = 4.dp)
    )
}
