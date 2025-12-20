package com.app.mobile.domain.usecase.hives.works

import com.app.mobile.domain.models.hives.WorkDomain

class CreateWorkUseCase() {
    suspend operator fun invoke(hiveId: String) = WorkDomain(
        hiveId = hiveId,
        title = "",
        text = ""
    )
}