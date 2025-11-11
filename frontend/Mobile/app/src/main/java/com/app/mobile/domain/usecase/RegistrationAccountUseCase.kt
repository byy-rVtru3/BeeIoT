package com.app.mobile.domain.usecase

import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.domain.models.registration.RegistrationRequestResult
import com.app.mobile.domain.repository.Repository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class RegistrationAccountUseCase(
    private val repository: Repository,
    private val dispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke(registrationModel: RegistrationModel):
            RegistrationRequestResult =
        withContext(dispatcher) {
            repository.registrationAccount(registrationModel)
        }

}