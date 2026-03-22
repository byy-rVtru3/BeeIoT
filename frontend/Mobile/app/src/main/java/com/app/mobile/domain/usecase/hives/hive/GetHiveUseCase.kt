package com.app.mobile.domain.usecase.hives.hive

import com.app.mobile.domain.repository.datasource.HivesDataSource

class GetHiveUseCase(private val hivesDataSource: HivesDataSource) {
    suspend operator fun invoke(name: String) = hivesDataSource.getHive(name)
}
