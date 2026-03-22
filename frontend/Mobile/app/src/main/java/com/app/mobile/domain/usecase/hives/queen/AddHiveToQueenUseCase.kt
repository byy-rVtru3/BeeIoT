package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.domain.repository.datasource.HivesDataSource

class AddHiveToQueenUseCase(private val hivesDataSource: HivesDataSource) {
    suspend operator fun invoke(hiveName: String, queenName: String) =
        hivesDataSource.linkQueenToHive(hiveName, queenName)
}
