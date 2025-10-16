package com.app.mobile.data.repository

import com.app.mobile.data.api.BeeApiClient
import com.app.mobile.data.api.models.authorization.AuthorizationResponseModel
import com.app.mobile.data.api.models.registration.RegistrationRequestApiModel
import com.app.mobile.data.converter.RegistrationResponseConverter
import com.app.mobile.domain.models.registration.RegistrationRequestResult
import com.app.mobile.domain.repository.Repository

class RepositoryImpl(
        private val beeApiClient: BeeApiClient,
        private val registrationResponseConverter: RegistrationResponseConverter
) : Repository {
    override suspend fun registrationAccount(
            registrationRequestApiModel: RegistrationRequestApiModel
    ): RegistrationRequestResult {
        val response = beeApiClient.registrationAccount(registrationRequestApiModel)
        return registrationResponseConverter.convert(response)
    }

    override suspend fun authorizationAccount(
            email: String,
            password: String
    ): AuthorizationResponseModel {
        TODO("Not yet implemented")
    }

    override suspend fun confirmationUser(userId: String, code: String): Boolean {
        TODO("Not yet implemented")
    }
}