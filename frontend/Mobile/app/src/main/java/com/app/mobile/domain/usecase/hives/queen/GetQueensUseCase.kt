package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.domain.repository.QueenRepository

class GetQueensUseCase(private val queenRepository: QueenRepository) {
    suspend operator fun invoke() = queenRepository.getQueens()
}
