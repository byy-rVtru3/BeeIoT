package com.app.mobile.data.repository

import com.app.mobile.data.api.models.authorization.AuthorizationResponseModel
import com.app.mobile.domain.models.registration.RegistrationResponseModel

interface Repository {
    suspend fun registrationAccount(email: String, password: String): RegistrationResponseModel
    suspend fun authorizationAccount(email: String, password: String): AuthorizationResponseModel
}