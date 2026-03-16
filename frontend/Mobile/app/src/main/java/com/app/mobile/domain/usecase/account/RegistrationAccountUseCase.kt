package com.app.mobile.domain.usecase.account

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.mappers.toUserDomain
import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.domain.repository.RepositoryApi
import com.app.mobile.domain.repository.UserLocalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RegistrationAccountUseCase(
    private val repositoryApi: RepositoryApi,
    private val userLocalRepository: UserLocalRepository,
    private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(registrationModel: RegistrationModel):
        ApiResult<Unit> =
        withContext(dispatcher) {
            val result = repositoryApi.registrationAccount(registrationModel)

            if (result is ApiResult.Success) {
                userLocalRepository.addUser(registrationModel.toUserDomain())
            }
            result
        }
}