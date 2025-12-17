package com.app.mobile.domain.models.hives.queen

import com.app.mobile.domain.models.DateRange

data class AdultStage(
    val emergence: DateRange,      // Выход
    val maturation: DateRange,     // Созревание
    val matingFlight: DateRange,   // Облет
    val insemination: DateRange,   // Осеменение (для ИО)
    val checkLaying: DateRange     // Проверка засева
)
