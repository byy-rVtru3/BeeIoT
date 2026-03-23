package com.app.mobile.domain.usecase.hives.hub

import com.app.mobile.domain.repository.datasource.HubDataSource

class GetHubWithSensorsUseCase(private val hubRepository: HubDataSource) {
    suspend operator fun invoke(hubId: String) = hubRepository.getHubWithSensors(hubId)
}
