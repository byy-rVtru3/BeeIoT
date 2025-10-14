package com.app.mobile.presentation.mappers

import com.app.mobile.domain.models.registration.RegistrationRequestModel
import com.app.mobile.presentation.models.RegistrationModelUi


fun RegistrationModelUi.toDomain() = RegistrationRequestModel(
    email = email,
    password = password,
)