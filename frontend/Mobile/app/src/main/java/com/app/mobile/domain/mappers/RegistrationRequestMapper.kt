package com.app.mobile.domain.mappers

import com.app.mobile.data.api.models.registration.RegistrationRequestApiModel
import com.app.mobile.domain.models.registration.RegistrationRequestModel

fun RegistrationRequestModel.toApiModel() =
    RegistrationRequestApiModel(
        email = this.email,
        password = this.password
    )