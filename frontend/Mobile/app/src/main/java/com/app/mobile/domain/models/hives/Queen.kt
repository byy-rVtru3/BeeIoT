package com.app.mobile.domain.models.hives

data class Queen(
    val id: Int,
    val hiveId: Int,
    val stage: QueenStageDomain
)
