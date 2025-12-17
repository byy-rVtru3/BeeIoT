package com.app.mobile.domain.mappers

import com.app.mobile.domain.models.authorization.AuthorizationModel
import com.app.mobile.domain.models.authorization.AuthorizationRequestResult
import com.app.mobile.presentation.models.account.AuthorizationModelUi
import com.app.mobile.presentation.models.account.AuthorizationResultUi

fun AuthorizationModelUi.toDomain() = AuthorizationModel(
    email = email,
    password = password
)

fun AuthorizationRequestResult.toUiModel() = when(this) {
    is AuthorizationRequestResult.Success -> AuthorizationResultUi.Success

    is AuthorizationRequestResult.BadRequestError -> AuthorizationResultUi.Error(
        "Некорректный запрос"
    )

    is AuthorizationRequestResult.UserNotFoundError -> AuthorizationResultUi.Error(
        "Пользователь с таким email не зарегистрирован или неверный пароль")

    is AuthorizationRequestResult.ServerError -> AuthorizationResultUi.Error(
        "Ошибка сервера"
    )

    is AuthorizationRequestResult.TimeoutError -> AuthorizationResultUi.Error(
        "Превышено время ожидания"
    )

    is AuthorizationRequestResult.UnknownError -> AuthorizationResultUi.Error(
        "Неизвестная ошибка"
    )
}