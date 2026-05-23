package com.app.mobile.domain.usecase.hives.hub

import com.app.mobile.domain.repository.datasource.HubDataSource

class GetHubByIdUseCase(private val hubDataSource: HubDataSource) {
    suspend operator fun invoke(hubId: String) =
        hubDataSource.getHub(hubId)
}
