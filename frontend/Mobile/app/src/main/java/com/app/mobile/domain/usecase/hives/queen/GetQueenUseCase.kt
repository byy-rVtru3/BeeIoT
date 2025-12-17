package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.domain.models.hives.queen.QueenDomain
import com.app.mobile.domain.repository.QueenLocalRepository

class GetQueenUseCase(private val queenLocalRepository: QueenLocalRepository) {
    suspend operator fun invoke(queenId: String): QueenDomain? =
        queenLocalRepository.getQueenById(queenId)
}