package com.app.mobile.domain.models.hives

import java.util.UUID

data class HiveEditorDomain(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val connectedHubId: String?,
    val connectedQueenId: String?
)
