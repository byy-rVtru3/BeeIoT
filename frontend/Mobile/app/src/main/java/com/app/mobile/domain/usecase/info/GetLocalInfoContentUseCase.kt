package com.app.mobile.domain.usecase.info

import com.app.mobile.domain.repository.InfoContentRepository

class GetLocalInfoContentUseCase(
    private val infoContentRepository: InfoContentRepository
) {
    suspend operator fun invoke() = infoContentRepository.getLocalContent()
}
