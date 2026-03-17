package com.app.mobile.domain.models.hives

data class HiveDomain(
    val name: String,
    val sensor: String?,
    val hubName: String?,
    val queenName: String?,
    val active: Boolean
)
