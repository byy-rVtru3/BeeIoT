package com.app.mobile.domain.usecase

import com.app.mobile.domain.mappers.toUserDomain
import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.domain.models.registration.RegistrationRequestResult
import com.app.mobile.domain.repository.RepositoryApi
import com.app.mobile.domain.repository.RepositoryDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class RegistrationAccountUseCase(
    private val repositoryApi: RepositoryApi,
    private val repositoryDatabase: RepositoryDatabase,
    private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(registrationModel: RegistrationModel):
        RegistrationRequestResult =
        withContext(dispatcher) {
            val addUserDeferred = async {
                repositoryDatabase.addUser(
                    registrationModel.toUserDomain()
                )
            }

            val registrationAccountDeferred = async {
                repositoryApi.registrationAccount(registrationModel)
            }

            addUserDeferred.await()
            registrationAccountDeferred.await()
        }

}