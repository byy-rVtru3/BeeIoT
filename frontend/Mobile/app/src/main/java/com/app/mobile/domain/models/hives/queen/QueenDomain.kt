package com.app.mobile.domain.models.hives.queen

data class QueenDomain(
    val id: String,
    val hiveId: String?,
    val name: String,
    val stages: QueenLifecycle
)
