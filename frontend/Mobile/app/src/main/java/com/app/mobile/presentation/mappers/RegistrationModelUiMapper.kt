package com.app.mobile.presentation.mappers

import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.domain.models.registration.RegistrationRequestModel
import com.app.mobile.presentation.models.RegistrationModelUi


fun RegistrationModelUi.toDomain() = RegistrationRequestModel(
    email = email,
    password = password,
)

fun RegistrationModel.toUiModel() = RegistrationModelUi(
    email = email,
    name = name,
    password = password,
    repeatPassword = repeatPassword
)