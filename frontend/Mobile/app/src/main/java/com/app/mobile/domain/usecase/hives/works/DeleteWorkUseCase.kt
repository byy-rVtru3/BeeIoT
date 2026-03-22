package com.app.mobile.domain.usecase.hives.works

import com.app.mobile.domain.repository.WorkLocalRepository

class DeleteWorkUseCase(private val workRepository: WorkLocalRepository) {
    suspend operator fun invoke(workId: String) = workRepository.deleteWork(workId)
}
