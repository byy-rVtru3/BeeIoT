package com.app.mobile.domain.usecase.hives

import com.app.mobile.domain.models.hives.QueenDomain
import com.app.mobile.domain.repository.HivesLocalRepository

class GetQueenUseCase(private val hivesLocalRepository: HivesLocalRepository) {
    suspend operator fun invoke(hiveId: Int): QueenDomain? =
        hivesLocalRepository.getQueenByHiveId(hiveId)
}