package com.app.mobile.presentation.models.queen

import com.app.mobile.presentation.models.hive.HivePreview
import java.time.LocalDate

data class QueenUiModel(
    val id: String,
    val hive: HivePreview?,
    val name: String,
    val timeline: List<TimelineItem>
)

data class TimelineItem(
    val date: LocalDate,        // Сырая дата для логики
    val dateFormatted: String,  // Готовая строка: "12 июня"
    val title: String,          // "Личинка: День 3"
    val description: String,    // "Кормление маточным молочком"
    val stageType: StageType,   // Для иконки
    val isToday: Boolean,       // Чтобы выделить текущий день
    val isCompleted: Boolean    // Чтобы "зачеркнуть" или затемнить прошедшее
)