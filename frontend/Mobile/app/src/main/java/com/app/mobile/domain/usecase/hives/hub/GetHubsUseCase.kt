package com.app.mobile.domain.usecase.hives.hub

import com.app.mobile.domain.repository.HubRepository

class GetHubsUseCase(private val hubRepository: HubRepository) {
    suspend operator fun invoke() = hubRepository.getHubs()
}
