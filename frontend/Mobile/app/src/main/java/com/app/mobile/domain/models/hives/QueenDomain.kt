package com.app.mobile.domain.models.hives

data class QueenDomain(
    val id: Int,
    val hiveId: Int,
    val name: String,
    val stage: QueenStageDomain
)
