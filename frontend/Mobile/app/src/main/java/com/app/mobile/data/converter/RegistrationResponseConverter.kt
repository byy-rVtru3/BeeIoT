package com.app.mobile.data.converter

import com.app.mobile.data.api.models.ResponseApiModel
import com.app.mobile.domain.models.registration.RegistrationRequestResult
import retrofit2.Response

class RegistrationResponseConverter {
    fun convert(response: Response<ResponseApiModel>): RegistrationRequestResult {
        return if (response.isSuccessful) {
            RegistrationRequestResult.Success
        } else {
            handleError(response)
        }
    }

    private fun handleError(response: Response<ResponseApiModel>): RegistrationRequestResult {
        return when (response.code()) {
            400 -> RegistrationRequestResult.BadRequestError
            409 -> RegistrationRequestResult.UserAlreadyExistsError
            500 -> RegistrationRequestResult.ServerError
            504 -> RegistrationRequestResult.TimeoutError
            else -> RegistrationRequestResult.UnknownError
        }
    }
}