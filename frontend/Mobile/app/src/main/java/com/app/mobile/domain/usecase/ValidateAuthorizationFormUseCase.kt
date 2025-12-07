package com.app.mobile.domain.usecase

import com.app.mobile.presentation.ui.screens.authorization.viewmodel.AuthorizationFormState
import com.app.mobile.presentation.validators.AuthorizationValidator
import com.app.mobile.presentation.validators.ValidationError
import com.app.mobile.presentation.validators.ValidationResult

class ValidateAuthorizationFormUseCase(
    private val validator: AuthorizationValidator
) {

    operator fun invoke(formState: AuthorizationFormState): AuthorizationFormState {
        val emailResult = validator.validateEmail(formState.email)
        val passwordResult = validator.validatePassword(formState.password)

        return formState.copy(
            emailError = emailResult.firstErrorOrNull(),
            passwordError = passwordResult.firstErrorOrNull()
        )
    }

    private fun ValidationResult.firstErrorOrNull(): ValidationError? {
        return when (this) {
            is ValidationResult.Error -> errors.firstOrNull()
            is ValidationResult.Valid -> null
        }
    }
}

