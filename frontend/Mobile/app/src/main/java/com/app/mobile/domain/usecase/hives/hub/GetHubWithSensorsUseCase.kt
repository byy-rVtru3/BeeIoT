package com.app.mobile.domain.usecase.hives.hub

import com.app.mobile.domain.repository.HubRepository

class GetHubWithSensorsUseCase(private val hubRepository: HubRepository) {
    suspend operator fun invoke(hubId: String) = hubRepository.getHubWithSensors(hubId)
}
