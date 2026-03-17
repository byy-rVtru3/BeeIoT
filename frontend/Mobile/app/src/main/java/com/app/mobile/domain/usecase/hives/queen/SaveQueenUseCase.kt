package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.domain.repository.QueenRepository
import java.time.LocalDate

class SaveQueenUseCase(private val queenRepository: QueenRepository) {
    suspend operator fun invoke(name: String, startDate: LocalDate) =
        queenRepository.createQueen(name, startDate)
}
