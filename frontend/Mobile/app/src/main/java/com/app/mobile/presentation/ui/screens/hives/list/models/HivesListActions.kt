package com.app.mobile.presentation.ui.screens.hives.list.models

data class HivesListActions(
    val onHiveClick: (Int) -> Unit,
    val onCreateHiveClick: () -> Unit
)
