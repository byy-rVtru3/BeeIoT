package com.app.mobile.domain.usecase.hives.works

import com.app.mobile.domain.repository.WorkLocalRepository

class GetWorksUseCase(private val workRepository: WorkLocalRepository) {
    suspend operator fun invoke(hiveId: String) = workRepository.getWorks(hiveId)
}