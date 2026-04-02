package com.app.mobile.domain.usecase.account

import com.app.mobile.data.api.models.ApiResult

class UpdateEmailUseCase {
    suspend operator fun invoke(@Suppress("UNUSED_PARAMETER") email: String): ApiResult<Unit> =
        ApiResult.HttpError(code = 501, errorBody = "Изменение email не поддерживается")
}
