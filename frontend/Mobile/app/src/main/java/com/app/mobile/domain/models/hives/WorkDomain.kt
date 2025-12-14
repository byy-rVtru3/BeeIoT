package com.app.mobile.domain.models.hives

import java.time.LocalDateTime
import java.util.UUID


data class WorkDomain(
    val id: String = UUID.randomUUID().toString(),
    val hiveId: String,
    val title: String,
    val text: String,
    val dateTime: LocalDateTime
)
