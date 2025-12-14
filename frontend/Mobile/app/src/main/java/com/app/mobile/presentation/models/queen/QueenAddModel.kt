package com.app.mobile.presentation.models.queen

import com.app.mobile.presentation.models.hive.HivePreview

data class QueenAddModel(
    val id: String,
    val name: String,
    val birthDate: Long,
    val hiveId: String?,
    val hives: List<HivePreview>
)
