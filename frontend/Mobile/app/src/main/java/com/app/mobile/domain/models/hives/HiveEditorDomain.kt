package com.app.mobile.domain.models.hives

data class HiveEditorDomain(
    val name: String,
    val connectedHubId: String?,
    val connectedQueenName: String?
)
