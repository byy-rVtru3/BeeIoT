package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.domain.repository.QueenLocalRepository

class AddHiveToQueenUseCase(private val queenLocalRepository: QueenLocalRepository) {
    suspend operator fun invoke(queenId: String, hiveId: String) =
        queenLocalRepository.addHiveToQueen(queenId, hiveId)
}