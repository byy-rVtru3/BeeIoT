package com.app.mobile.presentation.validators

/**
 * Валидаторы для экрана регистрации
 *
 * Registration screen содержит следующие поля:
 * - name (имя пользователя)
 * - email (электронная почта)
 * - password (пароль)
 * - repeatPassword (повтор пароля)
 */

// Валидатор для поля "Имя"
val nameField = formField {
    +FilterOnlyLettersAndSpaces
    +FilterTrimSpaces
    +FilterMaxLength(50)
    +NameValidator
    +MinLengthValidator(2, ValidationError.NameTooShortError)
    +MaxLengthValidator(50, ValidationError.NameTooLongError)
    +NameFormatter
}

// Валидатор для поля "Email"
val emailField = formField {
    +FilterEmailCharacters
    +FilterTrimSpaces
    +FilterMaxLength(100)
    +EmailValidator
    +EmailFormatter
}

// Валидатор для поля "Пароль"
val passwordField = formField {
    +FilterPasswordCharacters()
    +FilterMaxLength(50)
    +MinLengthValidator(8, ValidationError.PasswordTooShortError)
    +PasswordStrengthValidator
}

// Валидатор для поля "Повтор пароля"
// Примечание: для проверки совпадения паролей нужно создавать динамически
fun repeatPasswordField(originalPassword: String) = formField {
    +FilterPasswordCharacters()
    +FilterMaxLength(50)
    +PasswordMatchValidator(originalPassword)
}

/**
 * Класс для управления валидацией всех полей регистрации
 */
class RegistrationValidator {
    fun validateName(name: String): ValidationResult = nameField.process(name)

    fun validateEmail(email: String): ValidationResult = emailField.process(email)

    fun validatePassword(password: String): ValidationResult = passwordField.process(password)

    fun validateRepeatPassword(password: String, repeatPassword: String): ValidationResult =
        repeatPasswordField(password).process(repeatPassword)

}
