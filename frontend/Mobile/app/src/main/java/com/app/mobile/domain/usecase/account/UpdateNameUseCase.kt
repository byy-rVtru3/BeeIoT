package com.app.mobile.domain.usecase.account

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.repository.RepositoryApi

class UpdateNameUseCase(private val repositoryApi: RepositoryApi) {
    suspend operator fun invoke(name: String): ApiResult<Unit> =
        repositoryApi.updateName(name)
}
