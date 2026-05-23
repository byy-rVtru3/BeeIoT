package com.app.mobile.domain.usecase.info

import com.app.mobile.domain.repository.InfoContentRepository

class SyncInfoContentUseCase(
    private val infoContentRepository: InfoContentRepository
) {
    suspend operator fun invoke(force: Boolean = false) =
        infoContentRepository.syncContentIfNeeded(force)
}
