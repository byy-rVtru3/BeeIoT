package com.app.mobile.presentation.ui.screens.confirmation

import com.app.mobile.presentation.models.TypeConfirmationUi
import kotlinx.serialization.Serializable

@Serializable
data class ConfirmationRoute(val email: String, val type: TypeConfirmationUi)