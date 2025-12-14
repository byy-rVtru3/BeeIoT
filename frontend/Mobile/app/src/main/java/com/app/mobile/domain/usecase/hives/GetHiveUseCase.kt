package com.app.mobile.domain.usecase.hives

import com.app.mobile.domain.repository.HivesLocalRepository

class GetHiveUseCase(private val hivesLocalRepository: HivesLocalRepository) {
    suspend operator fun invoke(hiveId: String) = hivesLocalRepository.getHive(hiveId)
}