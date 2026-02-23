package com.app.mobile.presentation.ui.screens.registration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.mobile.R
import com.app.mobile.presentation.models.account.TypeConfirmationUi
import com.app.mobile.presentation.ui.components.ErrorMessage
import com.app.mobile.presentation.ui.components.FullScreenProgressIndicator
import com.app.mobile.presentation.ui.components.ObserveAsEvents
import com.app.mobile.presentation.ui.components.PasswordTextField
import com.app.mobile.presentation.ui.components.PrimaryButton
import com.app.mobile.presentation.ui.components.Title
import com.app.mobile.presentation.ui.components.ValidatedTextField
import com.app.mobile.presentation.ui.screens.registration.models.RegistrationActions
import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationEvent
import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationFormState
import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationUiState
import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationViewModel
import com.app.mobile.presentation.validators.ValidationError
import com.app.mobile.ui.theme.Dimens
import com.app.mobile.ui.theme.MobileTheme

@Composable
fun RegistrationScreen(
	registrationViewModel: RegistrationViewModel,
	onRegisterClick: (String, TypeConfirmationUi) -> Unit
) {
	val registrationUiState by registrationViewModel.uiState.collectAsStateWithLifecycle()
	val snackBarHostState = remember { SnackbarHostState() }

	LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
		registrationViewModel.createUserAccount()
	}

	ObserveAsEvents(registrationViewModel.event) { event ->
		when (event) {
			is RegistrationEvent.NavigateToConfirmation -> onRegisterClick(
				event.email,
				event.type
			)

			is RegistrationEvent.ShowSnackBar           -> {
				snackBarHostState.showSnackbar(
					message = event.message,
					duration = SnackbarDuration.Short
				)
			}
		}
	}

	when (val state = registrationUiState) {
		is RegistrationUiState.Loading -> FullScreenProgressIndicator()
		is RegistrationUiState.Error   -> ErrorMessage(message = state.message, registrationViewModel::resetError)

		is RegistrationUiState.Content -> {
			val formState = state.formState

			val actions = RegistrationActions(
				onEmailChange = registrationViewModel::onEmailChange,
				onNameChange = registrationViewModel::onNameChange,
				onPasswordChange = registrationViewModel::onPasswordChange,
				onRepeatPasswordChange = registrationViewModel::onRepeatPasswordChange,
				onRegisterClick = registrationViewModel::onRegisterClick
			)

			RegistrationContent(formState, snackBarHostState, actions)
		}
	}
}

@Composable
fun RegistrationContent(
	formState: RegistrationFormState,
	snackBarHostState: SnackbarHostState,
	actions: RegistrationActions
) {
	Scaffold(
		snackbarHost = { SnackbarHost(snackBarHostState) },
		contentWindowInsets = WindowInsets.safeDrawing,
	) { paddingValues ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues)
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
					text = stringResource(R.string.registration_title),
					modifier = Modifier.padding(top = Dimens.TitleTopPadding)
				)

				Column(
					modifier = Modifier.fillMaxWidth(),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.spacedBy(Dimens.ItemsSpacingSmall)
				) {
					RegistrationNameTextField(
						name = formState.name,
						nameError = formState.nameError,
						onNameChange = actions.onNameChange
					)

					RegistrationEmailTextField(
						email = formState.email,
						emailError = formState.emailError,
						onEmailChange = actions.onEmailChange
					)

					RegistrationPasswordTextField(
						password = formState.password,
						passwordError = formState.passwordError,
						onPasswordChange = actions.onPasswordChange
					)

					RegistrationRepeatPasswordTextField(
						repeatPassword = formState.repeatPassword,
						repeatPasswordError = formState.repeatPasswordError,
						onRepeatPasswordChange = actions.onRepeatPasswordChange
					)
				}

				RegistrationButton(onClick = actions.onRegisterClick)
			}
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
		error = repeatPasswordError,
		supportingText = stringResource(R.string.password_requirements)
	)
}

@Composable
fun RegistrationButton(onClick: () -> Unit) {
	PrimaryButton(
		text = stringResource(R.string.registration_button),
		onClick = onClick,
		modifier = Modifier
			.padding(
				horizontal = Dimens.ButtonHorizontalPadding
			)
			.padding(bottom = Dimens.ButtonSoloVerticalPadding)
	)
}

@Preview(showBackground = true)
@Composable
fun RegistrationContentPreview() {
	MobileTheme {
		val formState = RegistrationFormState()
		val actions = RegistrationActions(
			onEmailChange = {},
			onNameChange = {},
			onPasswordChange = {},
			onRepeatPasswordChange = {},
			onRegisterClick = {}
		)
		RegistrationContent(
			formState = formState,
			snackBarHostState = SnackbarHostState(),
			actions = actions
		)
	}
}
