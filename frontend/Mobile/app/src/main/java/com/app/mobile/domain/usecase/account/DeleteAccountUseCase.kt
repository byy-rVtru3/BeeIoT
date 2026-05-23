package com.app.mobile.domain.usecase.account

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.session.manager.SessionManager
import com.app.mobile.data.session.manager.TokenManager
import com.app.mobile.domain.repository.RepositoryApi

class DeleteAccountUseCase(
    private val repositoryApi: RepositoryApi,
    private val sessionManager: SessionManager,
    private val tokenManager: TokenManager
) {
    suspend operator fun invoke(): ApiResult<Unit> {
        return try {
            val result = repositoryApi.deleteAccount()
            if (result is ApiResult.Success) {
                sessionManager.clearSession()
                tokenManager.clearToken()
            }
            result
        } catch (e: Exception) {
            ApiResult.UnexpectedError(e)
        }
    }
}