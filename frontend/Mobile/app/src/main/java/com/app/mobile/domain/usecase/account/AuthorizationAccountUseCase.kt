package com.app.mobile.domain.usecase.account

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.session.manager.SessionManager
import com.app.mobile.data.session.manager.TokenManager
import com.app.mobile.domain.models.authorization.AuthorizationModel
import com.app.mobile.domain.repository.RepositoryApi
import com.app.mobile.domain.repository.UserLocalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class AuthorizationAccountUseCase(
    private val repositoryApi: RepositoryApi,
    private val userLocalRepository: UserLocalRepository,
    private val sessionManager: SessionManager,
    private val tokenManager: TokenManager,
    private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(authorizationModel: AuthorizationModel):
        ApiResult<String> = withContext(dispatcher) {
        val result = repositoryApi.authorizationAccount(authorizationModel)

        if (result is ApiResult.Success) {
            val userId = userLocalRepository.getUserIdByEmail(authorizationModel.email)
            tokenManager.saveToken(result.data)
            userId?.let {
                sessionManager.saveCurrentUser(it)
            }
        }
        result
    }
}
