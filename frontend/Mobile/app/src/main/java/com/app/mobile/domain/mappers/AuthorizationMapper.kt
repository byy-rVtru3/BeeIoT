package com.app.mobile.domain.mappers

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.api.models.HttpCode
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.domain.models.authorization.AuthorizationModel
import com.app.mobile.presentation.models.account.AuthorizationModelUi
import com.app.mobile.presentation.models.account.AuthorizationResultUi

fun AuthorizationModelUi.toDomain() = AuthorizationModel(
    email = email,
    password = password
)

fun ApiResult<String>.toAuthorizationUiModel() = when (this) {
    is ApiResult.Success -> AuthorizationResultUi.Success

    is ApiResult.HttpError -> when (code) {
        HttpCode.NOT_FOUND -> AuthorizationResultUi.Error(
            "Пользователь с таким email не зарегистрирован или неверный пароль"
        )
        else -> AuthorizationResultUi.Error(toErrorMessage())
    }

    else -> AuthorizationResultUi.Error(toErrorMessage())
}