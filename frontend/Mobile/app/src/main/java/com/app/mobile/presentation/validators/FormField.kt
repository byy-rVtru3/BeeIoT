package com.app.mobile.presentation.validators

// Основной класс для обработки полей формы
class FormField private constructor(
    private val filters: List<Filter> = emptyList(),
    private val validators: List<Validator> = emptyList(),
    private val formatter: Formatter = SimpleFormatter,
    val isOptional: Boolean = false
) {
    // Обработка данных: фильтрация -> валидация -> форматирование
    fun process(data: String): ValidationResult {
        val filtered = filters.fold(data) { res, filter -> filter.filter(res) }

        return if (filtered.isEmpty() && isOptional) {
            filtered.asValid()
        } else {
            validators
                .fold(ValidationResult.valid(filtered)) { res, validator ->
                    res.andThen(validator)
                }
                .map {
                    formatter.format(it)
                }
        }
    }

    companion object {
        fun build(
            filters: List<Filter>,
            validators: List<Validator>,
            formatter: Formatter,
            isOptional: Boolean
        ): FormField = FormField(filters, validators, formatter, isOptional)
    }
}

// Builder для FormField
class FormFieldBuilder {
    private val filters: MutableList<Filter> = mutableListOf()
    private val validators: MutableList<Validator> = mutableListOf()
    private var formatter: Formatter = SimpleFormatter
    private var isOptional: Boolean = false

    fun build(): FormField {
        // Для неопциональных полей добавляем NotEmptyValidator в начало списка
        val allValidators = if (!isOptional) {
            listOf(NotEmptyValidator) + validators
        } else {
            validators
        }

        val conditionalValidators = allValidators.map { it.withConditionalValidation() }

        return FormField.build(filters, conditionalValidators, formatter, isOptional)
    }

    operator fun Filter.unaryPlus(): FormFieldBuilder {
        filters.add(this)
        return this@FormFieldBuilder
    }

    operator fun Validator.unaryPlus(): FormFieldBuilder {
        validators.add(this)
        return this@FormFieldBuilder
    }

    operator fun Formatter.unaryPlus(): FormFieldBuilder {
        formatter = this
        return this@FormFieldBuilder
    }
}

// DSL функция для создания FormField
fun formField(lambda: FormFieldBuilder.() -> FormFieldBuilder): FormField {
    return FormFieldBuilder().lambda().build()
}
