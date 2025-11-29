package com.app.mobile.domain.models.hives


data class HiveDomain(
    val id: Int,
    val name: String,
    val connectedHub: Hub?,
    val notifications: List<NotificationDomain>?,
    val queen: Queen?,
    val works: List<WorkDomain>?
)
