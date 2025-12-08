package com.app.mobile.domain.usecase

import com.app.mobile.presentation.ui.screens.confirmation.viewmodel.ConfirmationFormState
import com.app.mobile.presentation.validators.ConfirmationValidator
import com.app.mobile.presentation.validators.firstErrorOrNull

class ValidateConfirmationFormUseCase(
    private val validator: ConfirmationValidator
) {

    operator fun invoke(formState: ConfirmationFormState): ConfirmationFormState {
        val codeResult = validator.validateCode(formState.code)

        return formState.copy(
            codeError = codeResult.firstErrorOrNull()
        )
    }
}
