package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.data.repository.QueenLocalRepositoryImpl

class GetQueensUseCase(private val queenLocalRepositoryImpl: QueenLocalRepositoryImpl) {
    suspend operator fun invoke() = queenLocalRepositoryImpl.getQueens()
}