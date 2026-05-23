package com.app.mobile.domain.mappers

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.presentation.models.account.DeleteResultUi

fun ApiResult<Unit>.toDeleteUiModel() = when (this) {
    is ApiResult.Success -> DeleteResultUi.Success
    else -> DeleteResultUi.Error(toErrorMessage())
}