package com.app.mobile.domain.usecase.hives.hive

import com.app.mobile.domain.repository.HivesRepository

class GetHivePreviewUseCase(private val hivesRepository: HivesRepository) {
    suspend operator fun invoke(name: String) = hivesRepository.getHive(name)
}
