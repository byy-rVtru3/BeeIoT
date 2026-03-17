package com.app.mobile.domain.usecase.hives.hive

import com.app.mobile.domain.repository.HivesRepository

class SaveHiveUseCase(private val hivesRepository: HivesRepository) {
    suspend operator fun invoke(name: String, isNew: Boolean) =
        if (isNew) hivesRepository.createHive(name)
        else hivesRepository.updateHive(oldName = name)
}
