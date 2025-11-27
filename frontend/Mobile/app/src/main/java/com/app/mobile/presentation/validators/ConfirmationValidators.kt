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
    +FilterOnlyDigits.withConditionalValidation()
    +FilterMaxLength(6).withConditionalValidation()
    +ExactLengthValidator(6).withConditionalValidation()
    +OnlyDigitsValidator.withConditionalValidation()
    +CodeFormatter(3)
}

// Альтернативный валидатор для 4-значного кода (если нужен)
val confirmationCode4Field = formField {
    +FilterOnlyDigits.withConditionalValidation()
    +FilterMaxLength(4).withConditionalValidation()
    +ExactLengthValidator(4).withConditionalValidation()
    +OnlyDigitsValidator.withConditionalValidation()
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
