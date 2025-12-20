package com.app.mobile.core.extensions

import java.time.LocalDate
import java.time.LocalDateTime

fun Long.toLocalDate(): LocalDate = LocalDate.ofEpochDay(this)

fun String.toLocalDate(): LocalDate = LocalDate.parse(this)

fun String.toLocalDateTime(): LocalDateTime = LocalDateTime.parse(this)

fun LocalDate.toDatabaseValue(): Long = this.toEpochDay()