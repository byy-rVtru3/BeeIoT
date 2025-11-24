package com.app.mobile.domain.usecase

import com.app.mobile.data.session.manager.SessionManager
import com.app.mobile.domain.models.UserDomain
import com.app.mobile.domain.repository.RepositoryDatabase

class GetAccountInfoUseCase(
    private val repositoryDatabase: RepositoryDatabase,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): UserDomain? {
        val userId = sessionManager.getCurrentUserId()
        return userId?.let {
            repositoryDatabase.getUserById(userId)
        }
    }
}