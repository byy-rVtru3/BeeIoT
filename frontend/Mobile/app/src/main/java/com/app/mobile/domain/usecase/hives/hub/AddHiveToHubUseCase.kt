package com.app.mobile.domain.usecase.hives.hub

import com.app.mobile.domain.repository.HubLocalRepository

class AddHiveToHubUseCase(private val hubLocalRepository: HubLocalRepository) {
    suspend operator fun invoke(hubId: String, hiveId: String) =
        hubLocalRepository.addHiveToHub(hubId, hiveId)
}