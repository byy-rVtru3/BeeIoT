package com.app.mobile.domain.usecase.hives.works

import com.app.mobile.domain.repository.WorkRepository

class GetWorksUseCase(private val workRepository: WorkRepository) {
    suspend operator fun invoke(hiveName: String) = workRepository.getWorks(hiveName)
}
