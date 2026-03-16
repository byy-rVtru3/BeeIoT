package com.app.mobile.data.repository

import com.app.mobile.data.session.manager.TokenManager

class AuthRepository(
    private val tokenManager: TokenManager
) {
    fun getToken(): String? = tokenManager.getToken()
}
