package com.app.mobile.presentation.ui.screens.queen.list.models

data class QueenListActions(
    val onQueenClick: (String) -> Unit,
    val onAddClick: () -> Unit
)
