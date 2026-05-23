package com.app.mobile.domain.usecase.hives.hub

import com.app.mobile.domain.repository.datasource.HivesDataSource

class AddHiveToHubUseCase(private val hivesDataSource: HivesDataSource) {
    suspend operator fun invoke(hiveName: String, hubId: String) =
        hivesDataSource.linkHubToHive(hiveName, hubId)
}
