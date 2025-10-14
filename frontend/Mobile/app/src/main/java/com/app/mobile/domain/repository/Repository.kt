package com.app.mobile.domain.repository

import com.app.mobile.data.api.models.authorization.AuthorizationResponseModel
import com.app.mobile.data.api.models.registration.RegistrationRequestApiModel
import com.app.mobile.domain.models.registration.RegistrationResponseModel

interface Repository {
    suspend fun registrationAccount(registrationRequestApiModel: RegistrationRequestApiModel): RegistrationResponseModel
    suspend fun authorizationAccount(email: String, password: String): AuthorizationResponseModel
}