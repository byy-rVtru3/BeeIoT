package com.app.mobile.domain.mappers

import com.app.mobile.domain.models.logout.LogoutRequestResult
import com.app.mobile.presentation.models.account.LogoutResultUi


fun LogoutRequestResult.toUiModel() = when (this) {
    is LogoutRequestResult.Success -> LogoutResultUi.Success

    is LogoutRequestResult.BadRequestError -> LogoutResultUi.Error("Некорректный запрос")

    is LogoutRequestResult.ServerError -> LogoutResultUi.Error("Ошибка сервера")

    is LogoutRequestResult.TimeoutError -> LogoutResultUi.Error("Превышено время ожидания")

    is LogoutRequestResult.UnknownError -> LogoutResultUi.Error("Неизвестная ошибка")

    is LogoutRequestResult.UnauthorizedError -> LogoutResultUi.Error("Пользователь не авторизован")
}