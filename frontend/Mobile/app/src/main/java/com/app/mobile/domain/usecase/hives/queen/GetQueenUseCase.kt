package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.domain.repository.datasource.QueenDataSource

class GetQueenUseCase(private val queenDataSource: QueenDataSource) {
    suspend operator fun invoke(name: String) = queenDataSource.getQueen(name)
}
