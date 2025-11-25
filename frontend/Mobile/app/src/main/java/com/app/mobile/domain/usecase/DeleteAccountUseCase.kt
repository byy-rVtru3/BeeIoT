package com.app.mobile.domain.usecase

import com.app.mobile.data.session.manager.SessionManager
import com.app.mobile.domain.models.delete.DeleteRequestResult
import com.app.mobile.domain.repository.RepositoryApi
import com.app.mobile.domain.repository.RepositoryDatabase

class DeleteAccountUseCase(
    private val repositoryApi: RepositoryApi,
    private val repositoryDatabase: RepositoryDatabase,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): DeleteRequestResult {
        return try {
            val userId = sessionManager.getCurrentUserId()
            userId?.let {
                val result = repositoryApi.deleteAccount()
                if (result is DeleteRequestResult.Success) {
                    repositoryDatabase.deleteUser(it)
                    sessionManager.clearSession()
                }
                result
            }
            DeleteRequestResult.UnknownError
        } catch (_: Exception) {
            DeleteRequestResult.UnknownError
        }
    }
}