package com.app.mobile.presentation.ui.screens.confirmation.viewmodel

import com.app.mobile.presentation.validators.BaseFormValidator
import com.app.mobile.presentation.validators.ConfirmationValidator
import com.app.mobile.presentation.validators.ValidationError
import com.app.mobile.presentation.validators.ValidationResult

/**
 * Helper-класс для валидации формы подтверждения
 * Инкапсулирует логику валидации, чтобы не загрязнять ViewModel
 */
class ConfirmationFormValidator(
    private val validator: ConfirmationValidator = ConfirmationValidator()
) : BaseFormValidator() {

    /**
     * Результат валидации формы подтверждения
     */
    data class FormValidationResult(
        val codeError: ValidationError? = null
    ) {
        /**
         * Проверка наличия хотя бы одной ошибки
         */
        fun hasErrors(): Boolean = codeError != null

        /**
         * Применяет результат валидации к FormState
         */
        fun applyTo(formState: ConfirmationFormState): ConfirmationFormState {
            return formState.copy(codeError = codeError)
        }
    }

    /**
     * Валидирует всю форму и возвращает результат
     */
    fun validateForm(formState: ConfirmationFormState): FormValidationResult {
        val codeResult = validator.validateCode(formState.code)

        return FormValidationResult(
            codeError = codeResult.firstErrorOrNull()
        )
    }

    /**
     * Валидирует форму и применяет ошибки
     * Возвращает: обновленный FormState и флаг наличия ошибок
     */
    fun validateAndApply(formState: ConfirmationFormState): Pair<ConfirmationFormState, Boolean> {
        val validationResult = validateForm(formState)
        val updatedFormState = validationResult.applyTo(formState)
        return updatedFormState to validationResult.hasErrors()
    }

    // ========== Методы для валидации отдельных полей (для onChange) ==========

    fun validateCode(code: String): ValidationResult =
        validator.validateCode(code)
}