package com.app.mobile.domain.usecase

import android.util.Log
import com.app.mobile.domain.models.authorization.AuthorizationModel
import com.app.mobile.domain.models.authorization.AuthorizationRequestResult
import com.app.mobile.domain.repository.Repository

class AuthorizationAccountUseCase(
    private val repository: Repository
) {
    suspend operator fun invoke(authorizationModel: AuthorizationModel): AuthorizationRequestResult {
        return when (val response = repository.authorizationAccount(authorizationModel)) {
            is AuthorizationRequestResult.Success -> {
                val token = response.token
                Log.w("AuthorizationUseCase", "JWToken: $token")
                response
            }

            else -> response
        }
    }
}
