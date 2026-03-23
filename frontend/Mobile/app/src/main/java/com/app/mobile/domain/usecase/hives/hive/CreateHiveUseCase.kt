package com.app.mobile.domain.usecase.hives.hive

import com.app.mobile.domain.models.hives.HiveEditorDomain

class CreateHiveUseCase {
    operator fun invoke() = HiveEditorDomain(
        name = "Улей",
        connectedHubId = null,
        connectedQueenName = null
    )
}
