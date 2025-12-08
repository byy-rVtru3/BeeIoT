package com.app.mobile.presentation.validators

/**
 * Базовый helper-класс для валидации форм
 * Содержит общую логику, которая переиспользуется во всех FormValidator'ах
 */
abstract class BaseFormValidator {

    /**
     * Extension функция для извлечения первой ошибки из ValidationResult
     */
    protected fun ValidationResult.firstErrorOrNull(): ValidationError? {
        return when (this) {
            is ValidationResult.Error -> errors.firstOrNull()
            is ValidationResult.Valid -> null
        }
    }
}