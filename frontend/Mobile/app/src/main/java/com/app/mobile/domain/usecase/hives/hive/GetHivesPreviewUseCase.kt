package com.app.mobile.domain.usecase.hives.hive

import com.app.mobile.domain.repository.datasource.HivesDataSource

class GetHivesPreviewUseCase(private val hivesDataSource: HivesDataSource) {
    suspend operator fun invoke() = hivesDataSource.getHives()
}
