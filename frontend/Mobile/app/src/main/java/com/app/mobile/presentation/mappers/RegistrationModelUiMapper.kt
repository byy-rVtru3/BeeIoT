package com.app.mobile.presentation.mappers

import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.presentation.models.RegistrationModelUi


fun RegistrationModelUi.toDomain() = RegistrationModel(
    userId = userId,
    name = name,
    email = email,
    password = password
)