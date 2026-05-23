package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.domain.repository.datasource.QueenDataSource

class GetQueensWithCalendarsUseCase(private val queenDataSource: QueenDataSource) {
	suspend operator fun invoke() = queenDataSource.getQueensWithCalendars()
}
