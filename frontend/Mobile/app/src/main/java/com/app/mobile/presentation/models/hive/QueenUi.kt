package com.app.mobile.presentation.models.hive

sealed interface QueenUi {
    data class Present(val name: String, val stage: QueenStageUi) : QueenUi
    data object Absent : QueenUi
}