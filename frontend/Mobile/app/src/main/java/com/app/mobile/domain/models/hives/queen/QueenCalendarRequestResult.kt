package com.app.mobile.domain.models.hives.queen

sealed interface QueenCalendarRequestResult {
    data class Success(val queenLifecycle: QueenLifecycle) : QueenCalendarRequestResult
    data class Error(val message: String) : QueenCalendarRequestResult
}