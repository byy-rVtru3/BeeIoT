package com.app.mobile.data.api.mappers

import com.app.mobile.data.api.models.registration.RegistrationResponseApiModel
import com.app.mobile.domain.models.registration.RegistrationRequestResult

fun RegistrationResponseApiModel.toDomain() = when (this.code) {
    201 -> RegistrationRequestResult.Success
    409 -> RegistrationRequestResult.UserAlreadyExists
    500 -> RegistrationRequestResult.ServerError
    else -> RegistrationRequestResult.UnknownError
}