package com.app.mobile.presentation.ui.screens.hives.list.models

data class HivesListActions(
    val onHiveClick: (String) -> Unit,
    val onCreateHiveClick: () -> Unit
)
