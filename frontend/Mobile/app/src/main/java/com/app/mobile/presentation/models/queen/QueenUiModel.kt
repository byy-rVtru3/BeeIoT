package com.app.mobile.presentation.models.queen

import java.time.LocalDate

data class QueenUiModel(
    val name: String,
    val timeline: List<TimelineItem>
)

data class TimelineItem(
    val date: LocalDate,
    val dateFormatted: String,
    val title: String,
    val description: String,
    val stageType: StageType,
    val isToday: Boolean,
    val isCompleted: Boolean
)
