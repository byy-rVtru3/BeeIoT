package com.app.mobile.domain.usecase.hives.hive

import com.app.mobile.domain.models.hives.HiveEditorDomain
import com.app.mobile.domain.repository.HivesLocalRepository

class SaveHiveUseCase(private val hivesLocalRepository: HivesLocalRepository) {
    suspend operator fun invoke(hive: HiveEditorDomain) = hivesLocalRepository.saveHive(hive)
}