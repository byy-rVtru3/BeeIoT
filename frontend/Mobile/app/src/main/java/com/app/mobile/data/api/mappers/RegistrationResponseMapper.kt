package com.app.mobile.data.api.mappers

import com.app.mobile.data.api.models.registration.RegistrationResponseApiModel
import com.app.mobile.domain.models.registration.RegistrationRequestResult
import com.app.mobile.domain.models.registration.RegistrationResponseModel

fun RegistrationResponseApiModel.toDomain(): RegistrationResponseModel {
    return RegistrationResponseModel(
        registrationRequestResult = mapToResult(this)
    )
}

private fun mapToResult(registrationResponseApiModel: RegistrationResponseApiModel): RegistrationRequestResult {
    return when (registrationResponseApiModel.code) {
        201 -> RegistrationRequestResult.Success
        409 -> RegistrationRequestResult.UserAlreadyExists
        500 -> RegistrationRequestResult.ServerError
        else -> RegistrationRequestResult.UnknownError
    }
}