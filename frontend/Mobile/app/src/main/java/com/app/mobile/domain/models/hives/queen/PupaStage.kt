package com.app.mobile.domain.models.hives.queen

import com.app.mobile.domain.models.DateRange
import java.time.LocalDate

data class PupaStage(
    val period: DateRange,
    val selectionDate: LocalDate // Критическая дата для пчеловода
)
