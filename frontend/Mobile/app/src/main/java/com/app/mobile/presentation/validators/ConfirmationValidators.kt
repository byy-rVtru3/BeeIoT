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
    +CodeFormatter(3)
}

// Альтернативный валидатор для 4-значного кода (если нужен)
val confirmationCode4Field = formField {
    +FilterOnlyDigits
    +FilterMaxLength(4)
    +ExactLengthValidator(4)
    +OnlyDigitsValidator
    +CodeFormatter(2)
}

/**
 * Класс для управления валидацией полей подтверждения
 */
class ConfirmationValidator {
    // По умолчанию используем 6-значный код
    private val codeLength: Int = 6

    fun validateCode(code: String): ValidationResult {
        return when (codeLength) {
            4 -> confirmationCode4Field.process(code)
            6 -> confirmationCodeField.process(code)
            else -> confirmationCodeField.process(code)
        }
    }

    fun isCodeValid(code: String): Boolean = validateCode(code).isValid()
}
