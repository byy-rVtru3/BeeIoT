package com.app.mobile.domain.usecase.hives.works

import com.app.mobile.domain.repository.WorkRepository

class DeleteWorkUseCase(private val workRepository: WorkRepository) {
    suspend operator fun invoke(workId: String) = workRepository.deleteWork(workId)
}
