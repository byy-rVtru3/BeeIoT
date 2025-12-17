package com.app.mobile.domain.models.hives

import java.util.UUID

data class HubDomain(
    val id: String = UUID.randomUUID().toString(),
    val hiveId: String?,
    val name: String,
    val ipAddress: String,
    val port: Int
)
