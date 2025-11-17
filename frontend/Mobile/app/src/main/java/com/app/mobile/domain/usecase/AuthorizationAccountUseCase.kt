package com.app.mobile.domain.usecase

import android.util.Log
import com.app.mobile.domain.models.authorization.AuthorizationModel
import com.app.mobile.domain.models.authorization.AuthorizationRequestResult
import com.app.mobile.domain.repository.RepositoryApi
import com.app.mobile.domain.repository.RepositoryDatabase

class AuthorizationAccountUseCase(
    private val repositoryApi: RepositoryApi,
    private val repositoryDatabase: RepositoryDatabase
) {
    suspend operator fun invoke(authorizationModel: AuthorizationModel): AuthorizationRequestResult {
        return when (val response = repositoryApi.authorizationAccount(authorizationModel)) {
            is AuthorizationRequestResult.Success -> {
                val token = response.token
                Log.w("token", token)
                repositoryDatabase.addTokenToUser(authorizationModel.email, token)
                response
            }

            else -> response
        }
    }
}
