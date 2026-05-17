package com.app.mobile.presentation.ui.screens.hive.list.models

data class HivesListActions(
    val onHiveClick: (String) -> Unit,
    val onCreateHiveClick: () -> Unit,
    val onDeleteHive: (String) -> Unit,
    val onArchiveHive: (String) -> Unit,
    val onUnarchiveHive: (String) -> Unit
)
