package com.app.mobile.domain.usecase.account

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.domain.repository.RepositoryApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RegistrationAccountUseCase(
    private val repositoryApi: RepositoryApi,
    private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(registrationModel: RegistrationModel):
        ApiResult<Unit> =
        withContext(dispatcher) {
            repositoryApi.registrationAccount(registrationModel)
        }
}