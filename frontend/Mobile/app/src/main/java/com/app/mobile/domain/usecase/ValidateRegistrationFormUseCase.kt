package com.app.mobile.domain.usecase

import com.app.mobile.presentation.ui.screens.registration.viewmodel.RegistrationFormState
import com.app.mobile.presentation.validators.RegistrationValidator
import com.app.mobile.presentation.validators.firstErrorOrNull

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
}
