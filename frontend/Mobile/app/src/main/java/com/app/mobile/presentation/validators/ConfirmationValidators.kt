package com.app.mobile.presentation.validators

/**
 * Валидаторы для экрана подтверждения
 *
 * Confirmation screen содержит следующие поля:
 * - code (код подтверждения)
 *
 * Код подтверждения обычно состоит из 6 цифр
 */

// Валидатор для поля "Код подтверждения"
// Код состоит из 6 цифр, форматируется как XXX-XXX
val confirmationCodeField = formField {
    +FilterOnlyDigits
    +FilterMaxLength(6)
    +ExactLengthValidator(6)
    +OnlyDigitsValidator
}



/**
 * Класс для управления валидацией полей подтверждения
 */
class ConfirmationValidator {

    fun validateCode(code: String): ValidationResult {
        return confirmationCodeField.process(code)
    }

}
