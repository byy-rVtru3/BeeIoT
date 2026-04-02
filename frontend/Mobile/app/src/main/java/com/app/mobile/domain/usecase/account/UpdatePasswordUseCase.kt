package com.app.mobile.domain.usecase.account

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.repository.RepositoryApi

class UpdatePasswordUseCase(private val repositoryApi: RepositoryApi) {
    suspend operator fun invoke(email: String, newPassword: String): ApiResult<Unit> =
        repositoryApi.initiatePasswordChange(email, newPassword)
}
