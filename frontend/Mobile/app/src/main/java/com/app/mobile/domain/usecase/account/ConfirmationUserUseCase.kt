package com.app.mobile.domain.usecase.account

import com.app.mobile.domain.models.confirmation.ConfirmationModel
import com.app.mobile.domain.models.confirmation.TypeConfirmation
import com.app.mobile.domain.repository.RepositoryApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ConfirmationUserUseCase(
    private val repositoryApi: RepositoryApi,
    private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(confirmationModel: ConfirmationModel) =
        withContext(dispatcher) {
            when (confirmationModel.type) {
                TypeConfirmation.REGISTRATION -> repositoryApi.confirmationUserRegistration(
                    confirmationModel
                )

                TypeConfirmation.RESET_PASSWORD -> repositoryApi.confirmationUserResetPassword(
                    confirmationModel
                )
            }
        }
}