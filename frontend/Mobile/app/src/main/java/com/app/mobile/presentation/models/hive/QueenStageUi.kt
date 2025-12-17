package com.app.mobile.presentation.models.hive

data class QueenStageUi(
    val title: String,          // "День 5" или "Завершено"
    val description: String,       // "Личинка: Кормление"
    val progress: Float,        // 0.0 ... 1.0 (для LinearProgressIndicator)
    val isActionRequired: Boolean, // Нужно ли внимание пчеловода сегодня
    val remainingDays: String   // "Осталось 20 дней"
)
