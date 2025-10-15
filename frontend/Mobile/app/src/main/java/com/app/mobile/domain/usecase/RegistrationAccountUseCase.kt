package com.app.mobile.domain.usecase

import com.app.mobile.domain.mappers.toApiModel
import com.app.mobile.domain.models.registration.RegistrationRequestModel
import com.app.mobile.domain.repository.Repository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RegistrationAccountUseCase(
    private val repository: Repository,
    private val dispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke(registrationRequestModel: RegistrationRequestModel) =
        withContext(dispatcher) {
            repository.registrationAccount(registrationRequestModel.toApiModel())
        }

}