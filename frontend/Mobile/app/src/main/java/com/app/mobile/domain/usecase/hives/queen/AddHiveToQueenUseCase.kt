package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.domain.repository.HivesRepository

class AddHiveToQueenUseCase(private val hivesRepository: HivesRepository) {
    suspend operator fun invoke(hiveName: String, queenName: String) =
        hivesRepository.linkQueenToHive(hiveName, queenName)
}
