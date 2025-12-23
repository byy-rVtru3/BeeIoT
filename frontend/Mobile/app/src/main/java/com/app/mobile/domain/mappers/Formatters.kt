package com.app.mobile.domain.mappers

import com.app.mobile.domain.models.confirmation.TypeConfirmation
import com.app.mobile.presentation.models.account.TypeConfirmationUi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val FORMATTED_DATE_TIME = "dd.MM.yyyy HH:mm:ss"


fun typeConfirmationFormatter(type: TypeConfirmationUi) = when (type) {
    TypeConfirmationUi.REGISTRATION -> TypeConfirmation.REGISTRATION
    TypeConfirmationUi.RESET_PASSWORD -> TypeConfirmation.RESET_PASSWORD
}

fun localDateTimeFormatter(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern(FORMATTED_DATE_TIME)

    return dateTime.format(formatter)
}

fun localDateTimeParser(dateTime: String) = LocalDateTime.parse(
    dateTime, DateTimeFormatter
        .ofPattern(FORMATTED_DATE_TIME)
)