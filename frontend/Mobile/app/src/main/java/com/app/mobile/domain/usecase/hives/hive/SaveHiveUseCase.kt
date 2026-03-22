package com.app.mobile.domain.usecase.hives.hive

import com.app.mobile.domain.repository.datasource.HivesDataSource

class SaveHiveUseCase(private val hivesDataSource: HivesDataSource) {
    suspend operator fun invoke(oldName: String?, newName: String) =
        if (oldName == null) hivesDataSource.createHive(newName)
        else hivesDataSource.updateHive(oldName = oldName, newName = newName)
}
