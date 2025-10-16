package com.app.mobile.data.converter

import com.app.mobile.data.api.mappers.toDomain
import com.app.mobile.data.api.models.registration.RegistrationResponseApiModel
import com.app.mobile.domain.models.registration.RegistrationRequestResult

class RegistrationResponseConverter {
    fun convert(registrationResponseApiModel: RegistrationResponseApiModel): RegistrationRequestResult {
        return registrationResponseApiModel.toDomain()
    }
}