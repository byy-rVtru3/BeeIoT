package com.app.mobile.presentation.validators

/**
 * Валидаторы для экрана авторизации
 *
 * Authorization screen содержит следующие поля:
 * - email (электронная почта)
 * - password (пароль)
 */

// Валидатор для поля "Email" (упрощённый, без строгой проверки формата)
val authEmailField = formField {
    +FilterEmailCharacters
    +FilterTrimSpaces
    +FilterMaxLength(100)
    +NotEmptyValidator
    +EmailValidator
    +EmailFormatter
}

// Валидатор для поля "Пароль" (упрощённый, только проверка на непустоту)
val authPasswordField = formField {
    +FilterPasswordCharacters()
    +FilterMaxLength(50)
    +NotEmptyValidator
    +MinLengthValidator(8, ValidationError.PasswordTooShortError)
}

/**
 * Класс для управления валидацией полей авторизации
 */
class AuthorizationValidator {
    fun validateEmail(email: String): ValidationResult = authEmailField.process(email)

    fun validatePassword(password: String): ValidationResult = authPasswordField.process(password)
}

