package com.app.mobile.domain.usecase

import com.app.mobile.domain.models.registration.RegistrationModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class CreateUserAccountUseCase(private val dispatcher: CoroutineDispatcher) {

    suspend operator fun invoke() = withContext(dispatcher) {
        RegistrationModel("", "", "", "")
    }
}