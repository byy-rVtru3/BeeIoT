package com.app.mobile.presentation.ui.screens.confirmation

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmationRoute(val email: String, val type: String)