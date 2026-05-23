package com.app.mobile.domain.usecase.hives.hub

import com.app.mobile.domain.repository.datasource.HubDataSource

class GetHubsUseCase(private val hubRepository: HubDataSource) {
    suspend operator fun invoke() = hubRepository.getHubs()
}
