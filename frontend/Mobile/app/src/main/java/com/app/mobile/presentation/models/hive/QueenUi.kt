package com.app.mobile.presentation.models.hive

import com.app.mobile.presentation.models.queen.QueenPreviewModel

sealed interface QueenUi {
    data class Present(val queen: QueenPreviewModel) : QueenUi
    data object Absent : QueenUi
}