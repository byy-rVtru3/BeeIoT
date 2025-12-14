package com.app.mobile.data.database.entity

import kotlinx.serialization.Serializable

@Serializable
data class QueenLifecycleDbModel(
    val egg: EggStageDb,
    val larva: LarvaStageDb,
    val pupa: PupaStageDb,
    val adult: AdultStageDb
)
