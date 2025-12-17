package com.app.mobile.core.extensions

import java.time.LocalDate

fun Long.toLocalDate(): LocalDate = LocalDate.ofEpochDay(this)

fun String.toLocalDate(): LocalDate = LocalDate.parse(this)

fun LocalDate.toDatabaseValue(): Long = this.toEpochDay()