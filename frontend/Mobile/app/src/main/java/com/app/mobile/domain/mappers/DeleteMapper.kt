package com.app.mobile.domain.mappers

import com.app.mobile.domain.models.delete.DeleteRequestResult
import com.app.mobile.presentation.models.account.DeleteResultUi

fun DeleteRequestResult.toUiModel() = when (this) {
    is DeleteRequestResult.Success -> DeleteResultUi.Success

    is DeleteRequestResult.UnauthorizedError -> DeleteResultUi.Error("Пользователь не авторизован")

    is DeleteRequestResult.BadRequestError -> DeleteResultUi.Error("Некорректный запрос")

    is DeleteRequestResult.ServerError -> DeleteResultUi.Error("Ошибка сервера")

    is DeleteRequestResult.TimeoutError -> DeleteResultUi.Error("Превышено время ожидания")

    is DeleteRequestResult.UnknownError -> DeleteResultUi.Error("Неизвестная ошибка")
}