package com.app.mobile.domain.usecase.hives.hub

import com.app.mobile.domain.mappers.toDomain
import com.app.mobile.domain.models.hives.HubDomain
import com.app.mobile.domain.repository.HubLocalRepository

class GetHubByIdUseCase(private val hubLocalRepository: HubLocalRepository) {
    suspend operator fun invoke(hubId: String): HubDomain? =
        hubLocalRepository.getHubById(hubId)?.toDomain()
}
