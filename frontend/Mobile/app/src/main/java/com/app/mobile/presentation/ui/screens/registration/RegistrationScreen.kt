package com.app.mobile.presentation.ui.screens.registration

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.app.mobile.R
import com.app.mobile.presentation.models.RegistrationModelUi
import com.app.mobile.presentation.models.TypeConfirmationUi
import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationNavigationEvent
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.PasswordTextField
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.components.ValidatedTextField
import com.app.mobile.presentation.ui.screens.registration.models.RegistrationActions
import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationUiState
import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationViewModel
import com.app.mobile.presentation.validators.ValidationError

@Composable
fun RegistrationScreen(
    registrationViewModel: RegistrationViewModel,
    onRegisterClick: (String, TypeConfirmationUi) -> Unit,
    onSettingsClick: () -> Unit
) {
    val registrationUiState by registrationViewModel.registrationUiState.observeAsState(
        RegistrationUiState.Loading
    )

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

    when (val state = registrationUiState) {
        is RegistrationUiState.Loading -> FullScreenProgressIndicator()
        is RegistrationUiState.Error -> ErrorMessage(message = state.message) {}
        is RegistrationUiState.Content -> {
            val registrationModelUi = state.registrationModelUi

            val actions = RegistrationActions(
                onEmailChange = registrationViewModel::onEmailChange,
                onNameChange = registrationViewModel::onNameChange,
                onPasswordChange = registrationViewModel::onPasswordChange,
                onRepeatPasswordChange = registrationViewModel::onRepeatPasswordChange,
                onRegisterClick = registrationViewModel::onRegisterClick
            )

            RegistrationContent(registrationModelUi, actions, onSettingsClick)
        }
    }
}

@Composable
fun RegistrationContent(
    registrationModelUi: RegistrationModelUi,
    actions: RegistrationActions,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(36.dp, 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        ) {
            Title(
                text = stringResource(R.string.registration_title),
                modifier = Modifier.padding(top = 52.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(20.dp)
            ) {
                RegistrationNameTextField(
                    name = registrationModelUi.name,
                    nameError = registrationModelUi.nameError,
                    onNameChange = actions.onNameChange
                )

                RegistrationEmailTextField(
                    email = registrationModelUi.email,
                    emailError = registrationModelUi.emailError,
                    onEmailChange = actions.onEmailChange
                )

                RegistrationPasswordTextField(
                    password = registrationModelUi.password,
                    passwordError = registrationModelUi.passwordError,
                    onPasswordChange = actions.onPasswordChange
                )

                RegistrationRepeatPasswordTextField(
                    repeatPassword = registrationModelUi.repeatPassword,
                    repeatPasswordError = registrationModelUi.repeatPasswordError,
                    onRepeatPasswordChange = actions.onRepeatPasswordChange
                )
            }

            RegistrationButton(onClick = actions.onRegisterClick)
        }

        // Кнопка DEV в правом верхнем углу (только для разработчиков)
        Button(
            onClick = {
                Log.d("RegistrationScreen", "DEV button clicked!")
                Toast.makeText(context, "Открываю настройки mock", Toast.LENGTH_SHORT).show()
                onSettingsClick()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            )
        ) {
            Text("MOCK", color = Color.White)
        }
    }
}

@Composable
fun RegistrationNameTextField(
    name: String,
    nameError: ValidationError?,
    onNameChange: (String) -> Unit
) {
    ValidatedTextField(
        value = name,
        onValueChange = onNameChange,
        placeholder = stringResource(R.string.name),
        error = nameError
    )
}

@Composable
fun RegistrationEmailTextField(
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
fun RegistrationPasswordTextField(
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
fun RegistrationRepeatPasswordTextField(
    repeatPassword: String,
    repeatPasswordError: ValidationError?,
    onRepeatPasswordChange: (String) -> Unit
) {
    PasswordTextField(
        value = repeatPassword,
        onValueChange = onRepeatPasswordChange,
        placeholder = stringResource(R.string.repeat_password),
        error = repeatPasswordError
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
