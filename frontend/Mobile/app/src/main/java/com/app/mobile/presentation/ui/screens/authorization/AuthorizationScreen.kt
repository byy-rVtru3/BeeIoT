package com.app.mobile.presentation.ui.screens.authorization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.mobile.presentation.models.AuthorizationModelUi
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.screens.authorization.models.AuthorizationActions
import com.app.mobile.presentation.ui.screens.authorization.viewmodel.AuthorizationNavigationEvent
import com.app.mobile.presentation.ui.screens.authorization.viewmodel.AuthorizationUiState
import com.app.mobile.presentation.ui.screens.authorization.viewmodel.AuthorizationViewModel
import com.app.mobile.presentation.validators.ValidationConfig

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

    val isValidationEnabled = remember { mutableStateOf(ValidationConfig.isValidationEnabled) }

    when (val state = authorizationUiState) {
        is AuthorizationUiState.Loading -> {
            FullScreenProgressIndicator()
        }

        is AuthorizationUiState.Error -> {
            ErrorMessage(message = state.message) {}
        }

        is AuthorizationUiState.Content -> {
            val actions = AuthorizationActions(
                onEmailChange = authorizationViewModel::onEmailChange,
                onPasswordChange = authorizationViewModel::onPasswordChange,
                onAuthorizeClick = authorizationViewModel::onAuthorizeClick,
                onRegistrationClick = authorizationViewModel::onRegistrationClick
            )

            // Оборачиваем контент в Box чтобы разместить FloatingActionButton поверх
            Box(modifier = Modifier.fillMaxSize()) {
                AuthorizationContent(
                    authorizationModelUi = state.authorizationModelUi,
                    actions = actions
                )

                DeveloperPanel(
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
    authorizationModelUi: AuthorizationModelUi,
    actions: AuthorizationActions
) {

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Title("Авторизация")

        OutlinedTextField(
            modifier = Modifier.padding(16.dp),
            value = authorizationModelUi.email,
            onValueChange = { actions.onEmailChange(it) },
            label = { Text(text = "Email") }
        )

        OutlinedTextField(
            value = authorizationModelUi.password,
            onValueChange = { actions.onPasswordChange(it) },
            label = { Text(text = "Пароль") }
        )

        Button(
            modifier = Modifier.padding(16.dp),
            onClick = { actions.onAuthorizeClick() }
        ) {
            Text(text = "Авторизация")
        }

        Button(
            onClick = { actions.onRegistrationClick() }
        ) {
            Text(text = "Регистрация")
        }
    }
}