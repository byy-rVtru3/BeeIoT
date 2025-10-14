package com.app.mobile.data.repository

import com.app.mobile.data.api.BeeApiClient
import com.app.mobile.data.api.models.authorization.AuthorizationResponseModel
import com.app.mobile.data.api.models.registration.RegistrationRequestApiModel
import com.app.mobile.domain.models.registration.RegistrationResponseModel
import com.app.mobile.domain.repository.Repository

class RepositoryImpl(
    private val beeApiClient: BeeApiClient,
): Repository {
    override suspend fun registrationAccount(
        registrationRequestApiModel: RegistrationRequestApiModel
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