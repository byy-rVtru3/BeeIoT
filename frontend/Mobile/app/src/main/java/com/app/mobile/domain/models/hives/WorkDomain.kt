package com.app.mobile.domain.models.hives

import java.time.LocalDateTime


data class WorkDomain(
    val id: Int,
    val hiveId: Int,
    val title: String,
    val text: String,
    val dateTime: LocalDateTime
)
