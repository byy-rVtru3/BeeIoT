package com.app.mobile.domain.usecase.account

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.session.manager.SessionManager
import com.app.mobile.data.session.manager.TokenManager
import com.app.mobile.domain.repository.RepositoryApi

class LogoutAccountUseCase(
    private val repositoryApi: RepositoryApi,
    private val tokenManager: TokenManager,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): ApiResult<Unit> {
        return try {
            val result = repositoryApi.logoutAccount()
            if (result is ApiResult.Success) {
                tokenManager.clearToken()
                sessionManager.clearSession()
            }
            result
        } catch (e: Exception) {
            ApiResult.UnexpectedError(e)
        }
    }
}
