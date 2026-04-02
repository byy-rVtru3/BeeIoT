package com.app.mobile.domain.usecase.account

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.domain.models.UserDomain
import com.app.mobile.domain.repository.RepositoryApi

class GetAccountInfoUseCase(
    private val repositoryApi: RepositoryApi
) {
    suspend operator fun invoke(): ApiResult<UserDomain> =
        repositoryApi.getAccountInfo()
}