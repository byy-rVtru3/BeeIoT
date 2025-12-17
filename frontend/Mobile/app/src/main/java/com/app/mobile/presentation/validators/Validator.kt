package com.app.mobile.presentation.validators

// Базовый интерфейс для ошибок валидации
sealed interface ValidationError {
    // Конкретные типы ошибок
    object EmptyFieldError : ValidationError
    object InvalidEmailError : ValidationError
    object PasswordTooShortError : ValidationError
    object PasswordTooWeakError : ValidationError
    object PasswordsNotMatchError : ValidationError
    object InvalidCodeFormatError : ValidationError
    object InvalidNameError : ValidationError
    object NameTooShortError : ValidationError
    object NameTooLongError : ValidationError
}


// Результат валидации
sealed class ValidationResult {
    abstract val data: String
    abstract fun isValid(): Boolean

    class Valid(override val data: String) : ValidationResult() {
        override fun isValid(): Boolean = true
    }

    class Error(override val data: String, val errors: List<ValidationError>) : ValidationResult() {
        override fun isValid(): Boolean = false
    }

    companion object {
        fun valid(value: String): ValidationResult = Valid(value)

        fun invalid(value: String, errors: List<ValidationError>): ValidationResult {
            assert(errors.isNotEmpty())
            return Error(value, errors)
        }

        fun invalid(value: String, error: ValidationError): ValidationResult {
            return Error(value, listOf(error))
        }
    }

    fun bind(anotherValidationFunction: (String) -> ValidationResult): ValidationResult {
        return when (this) {
            is Error -> {
                when (val res = anotherValidationFunction(data)) {
                    is Error -> invalid(res.data, this.errors + res.errors)
                    is Valid -> invalid(res.data, this.errors)
                }
            }
            is Valid -> anotherValidationFunction(data)
        }
    }

    fun andThen(anotherValidator: Validator): ValidationResult =
        bind { str: String -> anotherValidator.validate(str) }

    fun map(transform: (String) -> String): ValidationResult {
        return when (this) {
            is Valid -> valid(transform(data))
            is Error -> invalid(transform(data), errors)
        }
    }
}

// Extension функция для создания Valid результата
fun String.asValid(): ValidationResult = ValidationResult.valid(this)

/**
 * Extension функция для извлечения первой ошибки из результата валидации
 * Используется в Validate*FormUseCase для упрощения кода
 */
fun ValidationResult.firstErrorOrNull(): ValidationError? {
    return when (this) {
        is ValidationResult.Error -> errors.firstOrNull()
        is ValidationResult.Valid -> null
    }
}

//
// Базовый интерфейс валидатора - функциональный интерфейс
fun interface Validator {
    fun validate(data: String): ValidationResult
}

// Комплексный валидатор - цепочка валидаторов
open class ComplexValidator private constructor(private val validators: List<Validator>) :
    Validator {
    override fun validate(data: String) =
        validators.fold(ValidationResult.valid(data)) { res, validator -> res.andThen(validator) }

    companion object {
        fun build(validators: List<Validator>): ComplexValidator {
            return ComplexValidator(validators)
        }
    }
}

// Builder для комплексного валидатора
class ComplexValidatorBuilder {
    private val validators: MutableList<Validator> = mutableListOf()

    fun build(): ComplexValidator {
        return ComplexValidator.build(validators)
    }

    operator fun Validator.unaryPlus(): ComplexValidatorBuilder {
        validators.add(this)
        return this@ComplexValidatorBuilder
    }
}

// Конкретные валидаторы

// Валидатор - не пустое значение
val NotEmptyValidator = Validator { data ->
    if (data.isEmpty()) {
        ValidationResult.invalid(data, ValidationError.EmptyFieldError)
    } else {
        data.asValid()
    }
}

// Валидатор - точная длина
class ExactLengthValidator(private val length: Int) : Validator {
    override fun validate(data: String): ValidationResult {
        return if (data.length != length) {
            ValidationResult.invalid(data, ValidationError.InvalidCodeFormatError)
        } else {
            data.asValid()
        }
    }
}

// Валидатор - минимальная длина
class MinLengthValidator(
    private val minLength: Int,
    private val error: ValidationError = ValidationError.PasswordTooShortError
) : Validator {
    override fun validate(data: String): ValidationResult {
        return if (data.length < minLength) {
            ValidationResult.invalid(data, error)
        } else {
            data.asValid()
        }
    }
}

// Валидатор - максимальная длина
class MaxLengthValidator(
    private val maxLength: Int,
    private val error: ValidationError = ValidationError.InvalidNameError
) : Validator {
    override fun validate(data: String): ValidationResult {
        return if (data.length > maxLength) {
            ValidationResult.invalid(data, error)
        } else {
            data.asValid()
        }
    }
}

// Валидатор - только цифры
val OnlyDigitsValidator = Validator { data ->
    if (data.all { it.isDigit() }) {
        data.asValid()
    } else {
        ValidationResult.invalid(data, ValidationError.InvalidCodeFormatError)
    }
}

// Валидатор email
val EmailValidator = Validator { data ->
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    if (emailRegex.matches(data)) {
        data.asValid()
    } else {
        ValidationResult.invalid(data, ValidationError.InvalidEmailError)
    }
}

// Валидатор - имя (только буквы и пробелы)
val NameValidator = Validator { data ->
    if (data.all { it.isLetter() || it.isWhitespace() } && data.isNotBlank()) {
        data.asValid()
    } else {
        ValidationResult.invalid(data, ValidationError.InvalidNameError)
    }
}

// Валидатор - сложность пароля
val PasswordStrengthValidator = Validator { data ->
    val hasUpperCase = data.any { it.isUpperCase() }
    val hasLowerCase = data.any { it.isLowerCase() }
    val hasDigit = data.any { it.isDigit() }

    if (hasUpperCase && hasLowerCase && hasDigit) {
        data.asValid()
    } else {
        ValidationResult.invalid(data, ValidationError.PasswordTooWeakError)
    }
}

// Валидатор - совпадение паролей
class PasswordMatchValidator(private val originalPassword: String) : Validator {
    override fun validate(data: String): ValidationResult {
        return if (data == originalPassword) {
            data.asValid()
        } else {
            ValidationResult.invalid(data, ValidationError.PasswordsNotMatchError)
        }
    }
}

// ============================================================================
// Система отключения валидации (только для develop-режима)
// ============================================================================

/**
 * Интерфейс для источника данных о состоянии валидации
 * Реализуется MockDataSourceImpl в обеих версиях (develop и live)
 */
interface ValidationStateProvider {
    /**
     * Проверяет, включена ли валидация
     */
    fun isValidationEnabled(): Boolean

    /**
     * Устанавливает состояние валидации
     */
    fun setValidationEnabled(enabled: Boolean)
}

/**
 * Глобальная конфигурация валидации
 * Позволяет отключать валидацию в develop-режиме для ускорения тестирования
 *
 * ВАЖНО: Состояние сохраняется через ValidationStateProvider в SharedPreferences
 */
object ValidationConfig {
    /**
     * Включена ли валидация
     * В develop-версии можно отключить через UI
     * В live-версии всегда включена
     */
    var isValidationEnabled: Boolean = true
        private set

    private var stateProvider: ValidationStateProvider? = null

    /**
     * Инициализация с провайдером состояния для сохранения
     */
    fun init(provider: ValidationStateProvider) {
        stateProvider = provider
        // Загружаем сохраненное состояние
        isValidationEnabled = provider.isValidationEnabled()
    }

    /**
     * Отключает все валидации (только в develop)
     */
    fun disableValidation() {
        isValidationEnabled = false
        saveState()
    }

    /**
     * Включает все валидации
     */
    fun enableValidation() {
        isValidationEnabled = true
        saveState()
    }

    private fun saveState() {
        stateProvider?.setValidationEnabled(isValidationEnabled)
    }
}

/**
 * Wrapper для валидатора, который учитывает глобальный флаг
 */
class ConditionalValidator(private val validator: Validator) : Validator {
    override fun validate(data: String): ValidationResult {
        return if (ValidationConfig.isValidationEnabled) {
            validator.validate(data)
        } else {
            // Если валидация выключена, всегда возвращаем Valid
            ValidationResult.valid(data)
        }
    }
}

/**
 * Extension для создания условного валидатора
 */
fun Validator.withConditionalValidation(): Validator = ConditionalValidator(this)
