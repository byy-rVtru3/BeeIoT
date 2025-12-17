package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.domain.repository.QueenLocalRepository

class SaveQueenUseCase(
    private val queenLocalRepository: QueenLocalRepository
) {
    suspend operator fun invoke(queen: QueenDomain) =
        queenLocalRepository.saveQueen(queen)
}