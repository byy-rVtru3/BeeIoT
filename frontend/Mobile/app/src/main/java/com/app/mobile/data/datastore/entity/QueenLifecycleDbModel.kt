package com.app.mobile.data.datastore.entity

import kotlinx.serialization.Serializable

@Serializable
data class QueenLifecycleDbModel(
    val birthDate: Long,
    val egg: EggStageDb,
    val larva: LarvaStageDb,
    val pupa: PupaStageDb,
    val adult: AdultStageDb
)
