package com.app.mobile.domain.usecase.hives.hive

import com.app.mobile.domain.repository.HivesLocalRepository

class GetHivePreviewUseCase(private val hivesLocalRepository: HivesLocalRepository) {
    suspend operator fun invoke(hiveId: String) = hivesLocalRepository.getHivePreview(hiveId)
}