package com.app.mobile.presentation.ui.screens.authorization.viewmodel

import com.app.mobile.presentation.validators.AuthorizationValidator
import com.app.mobile.presentation.validators.ValidationError
import com.app.mobile.presentation.validators.ValidationResult
import com.app.mobile.presentation.validators.firstErrorOrNull

/**
 * Helper-класс для валидации формы авторизации
 * Инкапсулирует логику валидации, чтобы не загрязнять ViewModel
 */
class AuthorizationFormValidator(
    private val validator: AuthorizationValidator = AuthorizationValidator()
) {

    /**
     * Результат валидации формы авторизации
     */
    data class FormValidationResult(
        val emailError: ValidationError? = null,
        val passwordError: ValidationError? = null
    ) {
        /**
         * Проверка наличия хотя бы одной ошибки
         */
        fun hasErrors(): Boolean = emailError != null || passwordError != null

        /**
         * Применяет результат валидации к FormState
         */
        fun applyTo(formState: AuthorizationFormState): AuthorizationFormState {
            return formState.copy(
                emailError = emailError,
                passwordError = passwordError
            )
        }
    }

    /**
     * Валидирует всю форму и возвращает результат
     */
    fun validateForm(formState: AuthorizationFormState): FormValidationResult {
        val emailResult = validator.validateEmail(formState.email)
        val passwordResult = validator.validatePassword(formState.password)

        return FormValidationResult(
            emailError = emailResult.firstErrorOrNull(),
            passwordError = passwordResult.firstErrorOrNull()
        )
    }

    /**
     * Валидирует форму и применяет ошибки
     * Возвращает: обновленный FormState и флаг наличия ошибок
     */
    fun validateAndApply(formState: AuthorizationFormState): Pair<AuthorizationFormState, Boolean> {
        val validationResult = validateForm(formState)
        val updatedFormState = validationResult.applyTo(formState)
        return updatedFormState to validationResult.hasErrors()
    }

    // ========== Методы для валидации отдельных полей (для onChange) ==========

    fun validateEmail(email: String): ValidationResult =
        validator.validateEmail(email)

    fun validatePassword(password: String): ValidationResult =
        validator.validatePassword(password)
}