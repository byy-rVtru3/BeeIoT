package com.app.mobile.domain.usecase.hives.hive

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.repository.datasource.HivesDataSource

class DeleteHiveUseCase(private val hivesDataSource: HivesDataSource) {
    suspend operator fun invoke(name: String): ApiResult<Unit> =
        hivesDataSource.deleteHive(name)
}
