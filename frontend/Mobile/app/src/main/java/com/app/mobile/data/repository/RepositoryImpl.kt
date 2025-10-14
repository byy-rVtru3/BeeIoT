package com.app.mobile.data.repository

import com.app.mobile.data.api.BeeApiClient
import com.app.mobile.data.api.models.authorization.AuthorizationResponseModel
import com.app.mobile.data.converter.RegistrationRequestConverter
import com.app.mobile.domain.models.registration.RegistrationResponseModel
import com.app.mobile.domain.repository.Repository

class RepositoryImpl(
    private val beeApiClient: BeeApiClient,
    private val registrationRequestConverter: RegistrationRequestConverter,
): Repository {
    override suspend fun registrationAccount(
        email: String,
        password: String
    ): RegistrationResponseModel {
        TODO("Not yet implemented")
    }

    override suspend fun authorizationAccount(
        email: String,
        password: String
    ): AuthorizationResponseModel {
        TODO("Not yet implemented")
    }

}