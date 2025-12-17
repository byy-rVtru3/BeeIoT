package com.app.mobile.presentation.mappers

import com.app.mobile.domain.models.hives.queen.QueenLifecycle
import com.app.mobile.presentation.models.queen.StageType
import com.app.mobile.presentation.models.queen.TimelineItem
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Будем потом использовть DI для Locale, но пока оставим так
private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ru"))

fun QueenLifecycle.toTimelineUi(): List<TimelineItem> {
    val items = mutableListOf<TimelineItem>()
    val today = LocalDate.now()

    fun add(date: LocalDate, title: String, desc: String, type: StageType) {
        items.add(
            TimelineItem(
                date = date,
                dateFormatted = date.format(dateFormatter),
                title = title,
                description = desc,
                stageType = type,
                isToday = date.isEqual(today),
                isCompleted = date.isBefore(today)
            )
        )
    }

    // 1. Яйцо
    add(egg.day0Standing, "Яйцо: День 1", "Яйцо отложено и стоит вертикально", StageType.EGG)
    add(egg.day1Tilted, "Яйцо: День 2", "Яйцо наклоняется", StageType.EGG)
    add(egg.day2Lying, "Яйцо: День 3", "Яйцо лежит на дне ячейки", StageType.EGG)

    // 2. Личинка
    add(larva.hatchDate, "Личинка: Вылупление", "Из яйца вылупляется личинка", StageType.LARVA)
    larva.feedingDays.forEachIndexed { i, day ->
        add(day, "Личинка: День ${i + 1}", "Активное кормление маточным молочком", StageType.LARVA)
    }
    add(larva.sealedDate, "Запечатывание ячейки", "Рабочие пчёлы запечатывают ячейку", StageType.PUPA)

    // 3. Куколка
    add(pupa.period.start, "Куколка", "Начало стадии куколки", StageType.PUPA)
    add(pupa.selectionDate, "Отбор маточников", "Рекомендуемый день для отбора", StageType.ATTENTION)
    add(pupa.period.end, "Ожидание выхода", "Конец стадии куколки", StageType.PUPA)

    // 4. Взрослая особь
    add(adult.emergence.start, "Выход матки", "Начало периода выхода матки", StageType.QUEEN)
    add(adult.maturation.start, "Созревание", "Начало периода созревания", StageType.QUEEN)
    add(adult.matingFlight.start, "Брачный облёт", "Начало периода облётов", StageType.QUEEN)
    add(adult.insemination.start, "Осеменение", "Период откладки неоплодотворенных яиц", StageType.QUEEN)
    add(adult.checkLaying.start, "Проверка яйцекладки", "Начало проверки на засев", StageType.ATTENTION)
    add(adult.checkLaying.end, "Завершение проверки", "Конец периода проверки", StageType.QUEEN)

    return items
}