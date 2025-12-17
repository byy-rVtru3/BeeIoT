package com.app.mobile.domain.models.hives.queen

import java.time.LocalDate

data class EggStage(
    val day0Standing: LocalDate,
    val day1Tilted: LocalDate,
    val day2Lying: LocalDate
)
