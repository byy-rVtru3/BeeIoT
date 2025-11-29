package com.app.mobile.domain.mappers

import com.app.mobile.domain.models.confirmation.TypeConfirmation
import com.app.mobile.presentation.models.account.TypeConfirmationUi


fun typeConfirmationFormatter(type: TypeConfirmationUi) = when (type) {
    TypeConfirmationUi.REGISTRATION -> TypeConfirmation.REGISTRATION
    TypeConfirmationUi.RESET_PASSWORD -> TypeConfirmation.RESET_PASSWORD
}