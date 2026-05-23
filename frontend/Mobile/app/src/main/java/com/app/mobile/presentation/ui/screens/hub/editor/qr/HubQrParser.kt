package com.app.mobile.presentation.ui.screens.hub.editor.qr

sealed interface HubQrParseResult {
    data class Success(val hubId: String) : HubQrParseResult
    data object Invalid : HubQrParseResult
}

object HubQrParser {

    private const val PREFIX = "BEEIOT_HUB:"
    private val ALLOWED = Regex("^[A-Za-z0-9._\\-]{3,64}$")

    fun parse(rawPayload: String?): HubQrParseResult {
        val payload = rawPayload?.trim().orEmpty()
        if (payload.isEmpty()) return HubQrParseResult.Invalid

        val candidate = if (payload.startsWith(PREFIX, ignoreCase = true)) {
            payload.substring(PREFIX.length).trim()
        } else {
            payload
        }

        return if (ALLOWED.matches(candidate)) {
            HubQrParseResult.Success(candidate)
        } else {
            HubQrParseResult.Invalid
        }
    }
}
