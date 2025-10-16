package com.app.mobile.domain.repository

import com.app.mobile.data.api.models.authorization.AuthorizationResponseModel
import com.app.mobile.data.api.models.registration.RegistrationRequestApiModel
import com.app.mobile.domain.models.registration.RegistrationRequestResult

interface Repository {
    suspend fun registrationAccount(
        registrationRequestApiModel: RegistrationRequestApiModel): RegistrationRequestResult

    suspend fun authorizationAccount(email: String, password: String): AuthorizationResponseModel

    suspend fun confirmationUser(email: String, code: String, type: String): Boolean

    suspend fun resendConfirmationCode(email: String, type: String)
}