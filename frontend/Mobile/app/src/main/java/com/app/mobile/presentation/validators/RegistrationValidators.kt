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
    +FilterOnlyLettersAndSpaces.withConditionalValidation()
    +FilterTrimSpaces.withConditionalValidation()
    +FilterMaxLength(50).withConditionalValidation()
    +NameValidator.withConditionalValidation()
    +MinLengthValidator(2, NameTooShortError).withConditionalValidation()
    +MaxLengthValidator(50, NameTooLongError).withConditionalValidation()
    +NameFormatter
}

// Валидатор для поля "Email"
val emailField = formField {
    +FilterEmailCharacters.withConditionalValidation()
    +FilterTrimSpaces.withConditionalValidation()
    +FilterMaxLength(100).withConditionalValidation()
    +EmailValidator.withConditionalValidation()
    +EmailFormatter
}

// Валидатор для поля "Пароль"
val passwordField = formField {
    +FilterPasswordCharacters().withConditionalValidation()
    +FilterMaxLength(50).withConditionalValidation()
    +MinLengthValidator(8, PasswordTooShortError).withConditionalValidation()
    +PasswordStrengthValidator.withConditionalValidation()
}

// Валидатор для поля "Повтор пароля"
// Примечание: для проверки совпадения паролей нужно создавать динамически
fun repeatPasswordField(originalPassword: String) = formField {
    +FilterPasswordCharacters().withConditionalValidation()
    +FilterMaxLength(50).withConditionalValidation()
    +MinLengthValidator(8, PasswordTooShortError).withConditionalValidation()
    +PasswordMatchValidator(originalPassword).withConditionalValidation()
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
