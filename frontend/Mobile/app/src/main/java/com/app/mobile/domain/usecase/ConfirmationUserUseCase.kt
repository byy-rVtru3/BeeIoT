package com.app.mobile.domain.usecase

import com.app.mobile.domain.repository.Repository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ConfirmationUserUseCase(
    private val repository: Repository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(email: String, code: String, type: String) =
        withContext(dispatcher) {
            repository.confirmationUser(email, code, type)
        }
}