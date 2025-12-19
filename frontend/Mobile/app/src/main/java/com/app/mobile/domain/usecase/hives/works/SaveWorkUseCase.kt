package com.app.mobile.domain.usecase.hives.works

import com.app.mobile.domain.models.hives.WorkDomain
import com.app.mobile.domain.repository.WorkLocalRepository

class SaveWorkUseCase(private val workRepository: WorkLocalRepository) {
    suspend operator fun invoke(work: WorkDomain) = workRepository.saveWork(work)
}