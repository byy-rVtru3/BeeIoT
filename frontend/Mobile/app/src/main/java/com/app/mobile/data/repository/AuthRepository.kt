package com.app.mobile.data.repository

import com.app.mobile.data.session.manager.SessionManager
import com.app.mobile.domain.repository.UserLocalRepository

class AuthRepository(
    private val sessionManager: SessionManager,
    private val databaseImpl: UserLocalRepository
) {
    suspend fun getToken(): String? =
        sessionManager.getCurrentUserId()?.let { databaseImpl.getUserToken(it) }
}