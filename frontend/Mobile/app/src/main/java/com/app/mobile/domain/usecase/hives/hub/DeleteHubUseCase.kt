package com.app.mobile.domain.usecase.hives.hub

import com.app.mobile.domain.repository.datasource.HubDataSource

class DeleteHubUseCase(private val hubDataSource: HubDataSource) {
    suspend operator fun invoke(id: String) = hubDataSource.deleteHub(id)
}
