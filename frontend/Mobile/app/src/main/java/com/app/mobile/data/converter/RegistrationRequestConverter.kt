package com.app.mobile.data.converter

import com.app.mobile.data.api.models.registration.RegistrationRequestApiModel
import com.app.mobile.domain.models.registration.RegistrationRequestModel

class RegistrationRequestConverter {
    fun convert(registrationRequestModel: RegistrationRequestModel): RegistrationRequestApiModel {
        return RegistrationRequestApiModel(
            email = registrationRequestModel.email,
            password = registrationRequestModel.password,
        )
    }
}