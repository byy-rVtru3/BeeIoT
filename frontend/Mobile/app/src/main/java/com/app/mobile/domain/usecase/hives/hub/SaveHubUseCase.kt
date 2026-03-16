package com.app.mobile.domain.usecase.hives.hub

import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.domain.repository.HubLocalRepository

class SaveHubUseCase(private val hubLocalRepository: HubLocalRepository) {
    suspend operator fun invoke(hub: HubDomain) = hubLocalRepository.saveHub(hub)
}
