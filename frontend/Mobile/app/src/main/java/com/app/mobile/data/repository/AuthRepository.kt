package com.app.mobile.data.repository

import com.app.mobile.data.session.manager.SessionManager

class AuthRepository(
    private val sessionManager: SessionManager,
    private val databaseImpl: UserLocalRepositoryImpl
) {
    suspend fun getToken(): String? =
        sessionManager.getCurrentUserId()?.let { databaseImpl.getUserToken(it) }
}