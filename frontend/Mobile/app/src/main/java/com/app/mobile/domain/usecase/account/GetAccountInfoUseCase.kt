package com.app.mobile.domain.usecase.account

import com.app.mobile.data.session.manager.SessionManager
import com.app.mobile.domain.models.UserDomain
import com.app.mobile.domain.repository.UserLocalRepository

class GetAccountInfoUseCase(
    private val userLocalRepository: UserLocalRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(): UserDomain? {
        val userId = sessionManager.getCurrentUserId()
        return userId?.let {
            userLocalRepository.getUserById(userId)
        }
    }
}