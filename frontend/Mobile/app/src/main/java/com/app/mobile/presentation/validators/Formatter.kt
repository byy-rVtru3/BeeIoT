package com.app.mobile.presentation.validators

// Базовый интерфейс форматтера - функциональный интерфейс
fun interface Formatter {
    fun format(data: String): String
}

// Простой форматтер - возвращает данные без изменений
val SimpleFormatter = Formatter { it }

// Форматтер для email - приводит к нижнему регистру
val EmailFormatter = Formatter { it.lowercase() }

// Форматтер для имени - первая буква заглавная
val NameFormatter = Formatter { data ->
    data.trim().split(" ")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
}

// Форматтер для кода подтверждения - добавляет тире
class CodeFormatter(private val groupSize: Int = 3) : Formatter {
    override fun format(data: String): String {
        return data.chunked(groupSize).joinToString("-")
    }
}
