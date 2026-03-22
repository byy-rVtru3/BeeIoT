package com.app.mobile.domain.usecase.hives.hub

import com.app.mobile.domain.repository.HubRepository

class GetHubsWithSensorsUseCase(private val hubRepository: HubRepository) {
    suspend operator fun invoke() = hubRepository.getHubsWithSensors()
}
