package com.app.mobile.domain.models.hives.queen

import java.time.LocalDate
import java.util.UUID

data class QueenEditorDomain(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val birthDate: LocalDate,
    val hiveId: String?
)
