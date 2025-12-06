package com.app.mobile.domain.usecase

import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationFormState
import com.app.mobile.presentation.validators.ConfirmationValidator
import com.app.mobile.presentation.validators.ValidationError
import com.app.mobile.presentation.validators.ValidationResult

class ValidateConfirmationFormUseCase(
    private val validator: ConfirmationValidator
) {

    operator fun invoke(formState: ConfirmationFormState): ConfirmationFormState {
        val codeResult = validator.validateCode(formState.code)

        return formState.copy(
            codeError = codeResult.firstErrorOrNull()
        )
    }

    private fun ValidationResult.firstErrorOrNull(): ValidationError? {
        return when (this) {
            is ValidationResult.Error -> errors.firstOrNull()
            is ValidationResult.Valid -> null
        }
    }
}

