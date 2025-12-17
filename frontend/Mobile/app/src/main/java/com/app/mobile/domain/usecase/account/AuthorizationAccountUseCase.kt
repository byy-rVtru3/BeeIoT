package com.app.mobile.domain.usecase.account

import com.app.mobile.data.session.manager.SessionManager
import com.app.mobile.domain.models.authorization.AuthorizationModel
import com.app.mobile.domain.models.authorization.AuthorizationRequestResult
import com.app.mobile.domain.repository.RepositoryApi
import com.app.mobile.domain.repository.UserLocalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AuthorizationAccountUseCase(
    private val repositoryApi: RepositoryApi,
    private val userLocalRepository: UserLocalRepository,
    private val sessionManager: SessionManager,
    private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(authorizationModel: AuthorizationModel):
        AuthorizationRequestResult = withContext(dispatcher) {
        val result = repositoryApi.authorizationAccount(authorizationModel)

        if (result is AuthorizationRequestResult.Success) {
            val userId = userLocalRepository.addTokenToUser(authorizationModel.email, result.token)
            userId?.let {
                sessionManager.saveCurrentUser(it)
            } ?: AuthorizationRequestResult.UnknownError
        }
        result
    }
}
