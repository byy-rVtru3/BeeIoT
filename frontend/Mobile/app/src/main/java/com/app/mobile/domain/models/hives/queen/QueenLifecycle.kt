package com.app.mobile.domain.models.hives.queen

data class QueenLifecycle(
    val egg: EggStage,
    val larva: LarvaStage,
    val pupa: PupaStage,
    val adult: AdultStage
)