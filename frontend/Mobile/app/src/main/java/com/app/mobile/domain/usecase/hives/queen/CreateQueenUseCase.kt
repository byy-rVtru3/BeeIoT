package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.domain.models.hives.queen.QueenEditorDomain
import java.time.LocalDate

class CreateQueenUseCase {
    suspend operator fun invoke() = QueenEditorDomain(
        name = "Матка",
        birthDate = LocalDate.now(),
        hiveId = null
    )
}