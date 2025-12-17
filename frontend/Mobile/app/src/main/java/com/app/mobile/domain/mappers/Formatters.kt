package com.app.mobile.domain.mappers

import com.app.mobile.domain.models.confirmation.TypeConfirmation
import com.app.mobile.presentation.models.account.TypeConfirmationUi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun typeConfirmationFormatter(type: TypeConfirmationUi) = when (type) {
    TypeConfirmationUi.REGISTRATION -> TypeConfirmation.REGISTRATION
    TypeConfirmationUi.RESET_PASSWORD -> TypeConfirmation.RESET_PASSWORD
}

fun localDateTimeFormatter(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

    return dateTime.format(formatter)
}