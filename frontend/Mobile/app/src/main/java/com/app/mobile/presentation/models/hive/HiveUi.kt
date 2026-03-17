package com.app.mobile.presentation.models.hive

data class HiveUi(
    val name: String,
    val sensor: String?,
    val hubName: String?,
    val queenName: String?,
    val active: Boolean
)
