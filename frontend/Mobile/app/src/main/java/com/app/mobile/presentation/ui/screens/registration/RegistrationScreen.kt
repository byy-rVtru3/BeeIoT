package com.app.mobile.presentation.ui.screens.registration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.mobile.R
import com.app.mobile.presentation.models.RegistrationModelUi
import com.app.mobile.presentation.navigation.RegistrationNavigationEvent
import com.app.mobile.presentation.ui.components.CustomTextField
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.screens.registration.models.RegistrationActions
import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationUiState
import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationViewModel

@Composable
fun RegistrationScreen(
    registrationViewModel: RegistrationViewModel,
    onRegisterClick: (String, String) -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        registrationViewModel.createUserAccount()
    }

    val navigationEvent by registrationViewModel.navigationEvent.observeAsState()

    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { event ->
            when (event) {
                is RegistrationNavigationEvent.NavigateToConfirmation -> {
                    onRegisterClick(event.email, event.type)
                    registrationViewModel.onNavigationHandled()
                }
            }
        }
    }

    val registrationUiState by registrationViewModel.registrationUiState.observeAsState(
        RegistrationUiState.Loading
    )

    when (val state = registrationUiState) {
        is RegistrationUiState.Loading -> FullScreenProgressIndicator()
        is RegistrationUiState.Error -> ErrorMessage(message = state.message, onRetry = {})
        is RegistrationUiState.Content -> {
            val registrationModelUi = state.registrationModelUi

            val actions = RegistrationActions(
                onEmailChange = registrationViewModel::onEmailChange,
                onNameChange = registrationViewModel::onNameChange,
                onPasswordChange = registrationViewModel::onPasswordChange,
                onRepeatPasswordChange = registrationViewModel::onRepeatPasswordChange,
                onRegisterClick = registrationViewModel::onRegisterClick
            )

            RegistrationContent(registrationModelUi, actions)
        }
    }
}

@Composable
fun RegistrationContent(registrationModelUi: RegistrationModelUi, actions: RegistrationActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(36.dp, 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Title(
            text = stringResource(R.string.registration_title),
            modifier = Modifier.padding(bottom = 132.dp)
        )


        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 160.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegistrationNameTextField(registrationModelUi.name, actions.onNameChange)

            RegistrationEmailTextField(registrationModelUi.email, actions.onEmailChange)

            RegistrationPasswordTextField(registrationModelUi.password, actions.onPasswordChange)

            RegistrationRepeatPasswordTextField(registrationModelUi.repeatPassword, actions.onRepeatPasswordChange)
        }

        RegistrationButton(onClick = actions.onRegisterClick)

    }
}

@Composable
fun RegistrationEmailTextField(email: String, onEmailChange: (String) -> Unit) {
    CustomTextField(
        value = email,
        onValueChange = onEmailChange,
        placeholder = stringResource(R.string.email),
        modifier = Modifier.padding(bottom = 12.dp),
    )
}

@Composable
fun RegistrationNameTextField(
    name: String,
    onNameChange: (String) -> Unit
) {
    CustomTextField(
        value = name,
        onValueChange = onNameChange,
        modifier = Modifier.padding(bottom = 12.dp),
        placeholder = stringResource(R.string.name)
    )
}

@Composable
fun RegistrationPasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
) {
    CustomTextField(
        value = password,
        onValueChange = onPasswordChange,
        placeholder = stringResource(R.string.password),
        isPassword = true,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun RegistrationRepeatPasswordTextField(
    repeatPassword: String,
    onRepeatPasswordChange: (String) -> Unit
) {
    CustomTextField(
        value = repeatPassword,
        onValueChange = onRepeatPasswordChange,
        placeholder = stringResource(R.string.repeat_password),
        isPassword = true,
    )
}

@Composable
fun RegistrationButton(onClick: () -> Unit) {
    PrimaryButton(
        text = stringResource(R.string.registration_button),
        onClick = onClick,
        modifier = Modifier.padding(20.dp)
    )
}
