package com.app.mobile.domain.models.hives

import com.app.mobile.domain.models.hives.queen.QueenDomain
import java.util.UUID


data class HiveDomain(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val connectedHub: HubDomain?,
    val notifications: List<NotificationDomain>?,
    val queen: QueenDomain?,
    val works: List<WorkDomain>?
)
