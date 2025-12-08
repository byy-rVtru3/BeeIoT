package com.app.mobile.presentation.ui.screens.registration.viewmodel

import com.app.mobile.presentation.validators.RegistrationValidator
import com.app.mobile.presentation.validators.ValidationError
import com.app.mobile.presentation.validators.ValidationResult
import com.app.mobile.presentation.validators.firstErrorOrNull

/**
 * Helper-класс для валидации формы регистрации
 * Инкапсулирует логику валидации, чтобы не загрязнять ViewModel
 */
class RegistrationFormValidator(
    private val validator: RegistrationValidator = RegistrationValidator()
) {

    /**
     * Результат валидации формы регистрации
     */
    data class FormValidationResult(
        val nameError: ValidationError? = null,
        val emailError: ValidationError? = null,
        val passwordError: ValidationError? = null,
        val repeatPasswordError: ValidationError? = null
    ) {
        /**
         * Проверка наличия хотя бы одной ошибки
         */
        fun hasErrors(): Boolean =
            nameError != null ||
            emailError != null ||
            passwordError != null ||
            repeatPasswordError != null

        /**
         * Применяет результат валидации к FormState
         */
        fun applyTo(formState: RegistrationFormState): RegistrationFormState {
            return formState.copy(
                nameError = nameError,
                emailError = emailError,
                passwordError = passwordError,
                repeatPasswordError = repeatPasswordError
            )
        }
    }

    /**
     * Валидирует всю форму и возвращает результат
     */
    fun validateForm(formState: RegistrationFormState): FormValidationResult {
        val nameResult = validator.validateName(formState.name)
        val emailResult = validator.validateEmail(formState.email)
        val passwordResult = validator.validatePassword(formState.password)
        val repeatPasswordResult = validator.validateRepeatPassword(
            formState.password,
            formState.repeatPassword
        )

        return FormValidationResult(
            nameError = nameResult.firstErrorOrNull(),
            emailError = emailResult.firstErrorOrNull(),
            passwordError = passwordResult.firstErrorOrNull(),
            repeatPasswordError = repeatPasswordResult.firstErrorOrNull()
        )
    }

    /**
     * Валидирует форму и применяет ошибки
     * Возвращает: обновленный FormState и флаг наличия ошибок
     */
    fun validateAndApply(formState: RegistrationFormState): Pair<RegistrationFormState, Boolean> {
        val validationResult = validateForm(formState)
        val updatedFormState = validationResult.applyTo(formState)
        return updatedFormState to validationResult.hasErrors()
    }

    // ========== Методы для валидации отдельных полей (для onChange) ==========

    fun validateName(name: String): ValidationResult =
        validator.validateName(name)

    fun validateEmail(email: String): ValidationResult =
        validator.validateEmail(email)

    fun validatePassword(password: String): ValidationResult =
        validator.validatePassword(password)

    fun validateRepeatPassword(password: String, repeatPassword: String): ValidationResult =
        validator.validateRepeatPassword(password, repeatPassword)
}