package com.app.mobile.presentation.mappers

import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.presentation.models.account.RegistrationModelUi


fun RegistrationModelUi.toDomain() = RegistrationModel(
    name = name,
    email = email,
    password = password
)