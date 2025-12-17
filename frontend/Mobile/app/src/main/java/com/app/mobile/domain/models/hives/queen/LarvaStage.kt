package com.app.mobile.domain.models.hives.queen

import java.time.LocalDate

data class LarvaStage(
    val hatchDate: LocalDate,
    val feedingDays: List<LocalDate>,
    val sealedDate: LocalDate
)