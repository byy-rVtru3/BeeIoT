package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.domain.repository.QueenRepository

class GetQueenUseCase(private val queenRepository: QueenRepository) {
    suspend operator fun invoke(name: String) = queenRepository.getQueen(name)
}
