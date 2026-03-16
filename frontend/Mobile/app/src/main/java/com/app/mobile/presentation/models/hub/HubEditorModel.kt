package com.app.mobile.presentation.models.hub

data class HubEditorModel(
    val id: String,
    val hiveId: String?,
    val name: String,
    val ipAddress: String,
    val port: String
)
