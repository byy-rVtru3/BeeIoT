package com.app.mobile.domain.usecase.hives

import com.app.mobile.domain.repository.HivesLocalRepository

class GetHivesPreviewUseCase(private val hiveLocalRepository: HivesLocalRepository) {
    suspend operator fun invoke() = hiveLocalRepository.getHives()
}