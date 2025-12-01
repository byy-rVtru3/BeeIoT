package com.app.mobile.presentation.validators

// Базовый интерфейс фильтра - функциональный интерфейс
fun interface Filter {
    fun filter(data: String): String
}

/**
 * Условный фильтр - учитывает глобальный флаг ValidationConfig
 * Если валидация отключена, фильтр пропускает данные без изменений
 */
class ConditionalFilter(private val filter: Filter) : Filter {
    override fun filter(data: String): String {
        return if (ValidationConfig.isValidationEnabled) {
            filter.filter(data)
        } else {
            data  // Пропускаем данные как есть
        }
    }
}

/**
 * Extension для создания условного фильтра
 */
fun Filter.withConditionalValidation(): Filter = ConditionalFilter(this)

// Комплексный фильтр - цепочка фильтров
open class ComplexFilter private constructor(private val filters: List<Filter>) : Filter {
    override fun filter(data: String): String =
        filters.fold(data) { res, filter -> filter.filter(res) }

    companion object {
        fun build(filters: List<Filter>): ComplexFilter {
            return ComplexFilter(filters)
        }
    }
}

// Builder для комплексного фильтра
class ComplexFilterBuilder {
    private val filters: MutableList<Filter> = mutableListOf()

    fun build(): ComplexFilter {
        return ComplexFilter.build(filters)
    }

    operator fun Filter.unaryPlus(): ComplexFilterBuilder {
        filters.add(this)
        return this@ComplexFilterBuilder
    }
}

// DSL функция для создания фильтра
fun filter(lambda: ComplexFilterBuilder.() -> ComplexFilterBuilder): ComplexFilter {
    return ComplexFilterBuilder().lambda().build()
}

// Конкретные фильтры

// Фильтр только цифр
val FilterOnlyDigits = Filter { data -> data.filter { it.isDigit() } }

// Фильтр максимальной длины
class FilterMaxLength(private val maxLength: Int) : Filter {
    override fun filter(data: String): String = data.take(maxLength)
}

// Фильтр - удаление пробелов
val FilterTrimSpaces = Filter { it.trim() }

// Фильтр - только буквы и пробелы
val FilterOnlyLettersAndSpaces = Filter { data -> data.filter { it.isLetter() || it.isWhitespace() } }

// Фильтр - только email-совместимые символы
val FilterEmailCharacters = Filter { data ->
    data.filter { it.isLetterOrDigit() || it in "@.-_+" }
}

// Фильтр - удаление спецсимволов из пароля (оставляем только допустимые)
class FilterPasswordCharacters : Filter {
    override fun filter(data: String): String =
        data.filter { it.isLetterOrDigit() || it in "!@#$%^&*()_+-=[]{}|;:,.<>?" }
}
