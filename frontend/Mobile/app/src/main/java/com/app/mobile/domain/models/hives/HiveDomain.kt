package com.app.mobile.domain.models.hives

import com.app.mobile.domain.models.hives.queen.QueenDomain


data class HiveDomain(
    val id: String,
    val name: String,
    val connectedHub: HubDomain?,
    val notifications: List<NotificationDomain>,
    val queen: QueenDomain?,
    val works: List<WorkDomain>
)
