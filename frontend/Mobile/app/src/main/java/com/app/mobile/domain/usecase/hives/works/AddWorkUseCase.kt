package com.app.mobile.domain.usecase.hives.works

import com.app.mobile.domain.models.hives.WorkDomain
import com.app.mobile.domain.repository.WorkRepository

class AddWorkUseCase(private val workRepository: WorkRepository) {
    suspend operator fun invoke(work: WorkDomain) = workRepository.addWork(work)
}
