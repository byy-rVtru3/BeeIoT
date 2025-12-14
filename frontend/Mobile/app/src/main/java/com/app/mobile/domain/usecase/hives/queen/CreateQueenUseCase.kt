package com.app.mobile.domain.usecase.hives.queen

import com.app.mobile.domain.models.hives.queen.QueenAddDomain
import java.time.LocalDate

class CreateQueenUseCase {
    suspend operator fun invoke() = QueenAddDomain(
        name = "Матка",
        birthDate = LocalDate.now(),
        hiveId = null
    )
}