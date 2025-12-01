package com.app.mobile.presentation.ui.screens.hive.models

data class HiveActions(
    val onQueenClick: () -> Unit,
    val onWorkClick: () -> Unit,
    val onNotificationClick: () -> Unit,
    val onTemperatureClick: () -> Unit,
    val onNoiseClick: () -> Unit,
    val onWeightClick: () -> Unit,
    val onHiveListClick: () -> Unit,
    val onHiveEditClick: () -> Unit,
    val onDeleteClick: () -> Unit
)
