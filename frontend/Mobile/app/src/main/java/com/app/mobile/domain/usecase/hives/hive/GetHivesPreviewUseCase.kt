package com.app.mobile.domain.usecase.hives.hive

import com.app.mobile.domain.repository.HivesRepository

class GetHivesPreviewUseCase(private val hivesRepository: HivesRepository) {
    suspend operator fun invoke() = hivesRepository.getHives()
}
