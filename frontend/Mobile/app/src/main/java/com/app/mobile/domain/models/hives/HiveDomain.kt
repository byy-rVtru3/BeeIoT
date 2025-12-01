package com.app.mobile.domain.models.hives


data class HiveDomain(
    val id: Int,
    val name: String,
    val connectedHub: HubDomain?,
    val notifications: List<NotificationDomain>?,
    val queen: QueenDomain?,
    val works: List<WorkDomain>?
)
