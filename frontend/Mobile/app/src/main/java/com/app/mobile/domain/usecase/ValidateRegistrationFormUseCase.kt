package com.app.mobile.domain.usecase

import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationFormState
import com.app.mobile.presentation.validators.RegistrationValidator
import com.app.mobile.presentation.validators.ValidationError
import com.app.mobile.presentation.validators.ValidationResult


class ValidateRegistrationFormUseCase(
    private val validator: RegistrationValidator
) {

    operator fun invoke(formState: RegistrationFormState): RegistrationFormState {
        val nameResult = validator.validateName(formState.name)
        val emailResult = validator.validateEmail(formState.email)
        val passwordResult = validator.validatePassword(formState.password)
        val repeatPasswordResult = validator.validateRepeatPassword(
            formState.password,
            formState.repeatPassword
        )

        return formState.copy(
            nameError = nameResult.firstErrorOrNull(),
            emailError = emailResult.firstErrorOrNull(),
            passwordError = passwordResult.firstErrorOrNull(),
            repeatPasswordError = repeatPasswordResult.firstErrorOrNull()
        )
    }

    private fun ValidationResult.firstErrorOrNull(): ValidationError? {
        return when (this) {
            is ValidationResult.Error -> errors.firstOrNull()
            is ValidationResult.Valid -> null
        }
    }
}

