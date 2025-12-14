package com.app.mobile.domain.models.hives.queen

import java.time.LocalDate

data class QueenLifecycle(
    val birthDate: LocalDate,
    val egg: EggStage,
    val larva: LarvaStage,
    val pupa: PupaStage,
    val adult: AdultStage
)