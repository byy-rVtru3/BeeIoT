package com.app.mobile.presentation.mappers

import com.app.mobile.domain.models.hives.queen.QueenLifecycle
import com.app.mobile.presentation.models.hive.QueenStageUi
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun QueenLifecycle.toCurrentStageUi(today: LocalDate = LocalDate.now()): QueenStageUi {
    fun daysBetween(start: LocalDate, end: LocalDate) =
        ChronoUnit.DAYS.between(start, end).toInt() + 1

    val pupaEnd = pupa.period.end
    val remainingText = "Осталось ${ChronoUnit.DAYS.between(today, pupaEnd)} дней"

    return when {
        // Стадия яйца
        !today.isAfter(egg.day2Lying) -> {
            val current = daysBetween(egg.day0Standing, today)
            val total = daysBetween(egg.day0Standing, egg.day2Lying)
            QueenStageUi(
                title = "День $current",
                description = "Стадия яйца",
                progress = current.toFloat() / total,
                isActionRequired = false,
                remainingDays = remainingText
            )
        }
        // Стадия личинки
        !today.isAfter(larva.sealedDate) -> {
            val current = daysBetween(larva.hatchDate, today)
            val total = daysBetween(larva.hatchDate, larva.sealedDate)
            QueenStageUi(
                title = "День $current",
                description = "Стадия личинки",
                progress = current.toFloat() / total,
                isActionRequired = false,
                remainingDays = remainingText
            )
        }
        // Стадия куколки
        !today.isAfter(pupaEnd) -> {
            val current = daysBetween(pupa.period.start, today)
            val total = daysBetween(pupa.period.start, pupaEnd)
            QueenStageUi(
                title = "День $current",
                description = "Стадия куколки",
                progress = current.toFloat() / total,
                isActionRequired = today.isEqual(pupa.selectionDate),
                remainingDays = remainingText
            )
        }
        // Стадия взрослой особи
        !today.isAfter(adult.checkLaying.end) -> {
            val (desc, action) = getAdultStageDescription(today)
            val current = daysBetween(adult.emergence.start, today)
            val total = daysBetween(adult.emergence.start, adult.checkLaying.end)

            QueenStageUi(
                title = "День $current",
                description = desc,
                progress = current.toFloat() / total,
                isActionRequired = action,
                remainingDays = "Осталось ${
                    ChronoUnit.DAYS.between(
                        today,
                        adult.checkLaying.end
                    )
                } дней"
            )
        }
        // Завершено
        else -> QueenStageUi(
            title = "Завершено",
            description = "Матка успешно развилась",
            progress = 1.0f,
            isActionRequired = false,
            remainingDays = "Цикл завершён"
        )
    }
}

private fun QueenLifecycle.getAdultStageDescription(today: LocalDate): Pair<String, Boolean> {
    return when {
        today.isBefore(adult.maturation.start) -> "Выход из маточника" to false
        today.isBefore(adult.matingFlight.start) -> "Созревание" to false
        today.isBefore(adult.insemination.start) -> "Брачный облёт" to false
        today.isBefore(adult.checkLaying.start) -> "Осеменение" to false
        else -> "Проверка яйцекладки" to true
    }
}