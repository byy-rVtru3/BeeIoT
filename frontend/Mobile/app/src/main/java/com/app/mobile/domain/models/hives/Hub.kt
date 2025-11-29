package com.app.mobile.domain.models.hives

data class Hub(
    val id: Int,
    val hiveId: Int?,
    val name: String,
    val ipAddress: String,
    val port: Int
)
