package com.app.mobile.domain.usecase.account

import com.app.mobile.data.session.manager.SessionManager
import com.app.mobile.domain.models.logout.LogoutRequestResult
import com.app.mobile.domain.repository.RepositoryApi
import com.app.mobile.domain.repository.UserLocalRepository

class LogoutAccountUseCase(
    private val repositoryApi: RepositoryApi,
    private val userLocalRepository: UserLocalRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): LogoutRequestResult {
        return try {
            val userId = sessionManager.getCurrentUserId() ?: return LogoutRequestResult.UnknownError
            val result = repositoryApi.logoutAccount()
            if (result is LogoutRequestResult.Success) {
                userLocalRepository.deleteTokenFromUser(userId)
                sessionManager.clearSession()
            }
            result
        } catch (_: Exception) {
            LogoutRequestResult.UnknownError
        }
    }
}