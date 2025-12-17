package com.app.mobile.domain.mappers

import com.app.mobile.domain.models.confirmation.ConfirmationModel
import com.app.mobile.domain.models.confirmation.ConfirmationRequestResult
import com.app.mobile.presentation.models.account.ConfirmationModelUi
import com.app.mobile.presentation.models.account.ConfirmationResultUi


fun ConfirmationModelUi.toDomain() = ConfirmationModel(
    email = email,
    code = code.filter { it != '-' }, // так не должно быть думаю что надо фиксить в UI
    type = typeConfirmationFormatter(type)
)

fun ConfirmationRequestResult.toUiModel() = when (this) {
    is ConfirmationRequestResult.Success -> ConfirmationResultUi.Success

    is ConfirmationRequestResult.UnauthorizedError -> ConfirmationResultUi.Error(
        "Неверный код подтверждения или истек срок действия."
    )

    is ConfirmationRequestResult.ServerError -> ConfirmationResultUi.Error(
        "Ошибка сервера"
    )

    is ConfirmationRequestResult.TimeoutError  -> ConfirmationResultUi.Error(
        "Превышено время ожидания"
    )

    is ConfirmationRequestResult.BadRequestError -> ConfirmationResultUi.Error(
        "Некорректный запрос"
    )

    is ConfirmationRequestResult.NotFoundError -> ConfirmationResultUi.Error(
        "Пользователь с таким email не зарегистрирован"
    )

    is ConfirmationRequestResult.UnknownError -> ConfirmationResultUi.Error(
        "Неизвестная ошибка"
    )

}