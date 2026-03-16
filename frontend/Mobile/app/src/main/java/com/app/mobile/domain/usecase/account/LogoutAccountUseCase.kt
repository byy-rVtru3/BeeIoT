package com.app.mobile.domain.usecase.account

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.session.manager.SessionManager
import com.app.mobile.domain.repository.RepositoryApi
import com.app.mobile.domain.repository.UserLocalRepository

class LogoutAccountUseCase(
    private val repositoryApi: RepositoryApi,
    private val userLocalRepository: UserLocalRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): ApiResult<Unit> {
        return try {
            val userId = sessionManager.getCurrentUserId()
                ?: return ApiResult.UnexpectedError(IllegalStateException("User not found"))
            val result = repositoryApi.logoutAccount()
            if (result is ApiResult.Success) {
                userLocalRepository.deleteTokenFromUser(userId)
                sessionManager.clearSession()
            }
            result
        } catch (e: Exception) {
            ApiResult.UnexpectedError(e)
        }
    }
}