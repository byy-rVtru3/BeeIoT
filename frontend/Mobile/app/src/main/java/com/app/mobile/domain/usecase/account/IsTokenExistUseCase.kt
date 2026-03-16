package com.app.mobile.domain.usecase.account

import com.app.mobile.data.session.manager.TokenManager

class IsTokenExistUseCase(
    private val tokenManager: TokenManager
) {
    operator fun invoke(): Boolean = tokenManager.getToken() != null
}
