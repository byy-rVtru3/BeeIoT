package com.app.mobile.domain.mappers

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.presentation.models.account.LogoutResultUi


fun ApiResult<Unit>.toLogoutUiModel() = when (this) {
    is ApiResult.Success -> LogoutResultUi.Success
    else -> LogoutResultUi.Error(toErrorMessage())
}