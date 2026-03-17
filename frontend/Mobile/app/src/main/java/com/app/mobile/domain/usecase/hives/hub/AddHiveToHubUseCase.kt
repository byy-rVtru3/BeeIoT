package com.app.mobile.domain.usecase.hives.hub

import com.app.mobile.domain.repository.HivesRepository

class AddHiveToHubUseCase(private val hivesRepository: HivesRepository) {
    suspend operator fun invoke(hiveName: String, hubId: String) =
        hivesRepository.linkHubToHive(hiveName, hubId)
}
