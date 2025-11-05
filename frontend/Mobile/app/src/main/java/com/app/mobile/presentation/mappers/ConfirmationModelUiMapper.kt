package com.app.mobile.presentation.mappers

import com.app.mobile.domain.models.confirmation.ConfirmationModel
import com.app.mobile.presentation.models.ConfirmationModelUi


fun ConfirmationModelUi.toDomain() = ConfirmationModel(
    email = email,
    code = code,
    type = type // заебался фикс
)