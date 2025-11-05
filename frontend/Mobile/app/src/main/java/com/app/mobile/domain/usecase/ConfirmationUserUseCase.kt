package com.app.mobile.domain.usecase

import com.app.mobile.domain.models.confirmation.ConfirmationModel
import com.app.mobile.domain.repository.Repository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ConfirmationUserUseCase(
    private val repository: Repository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(confirmationModel: ConfirmationModel) =
        withContext(dispatcher) {
            repository.confirmationUser(confirmationModel)
        }
}