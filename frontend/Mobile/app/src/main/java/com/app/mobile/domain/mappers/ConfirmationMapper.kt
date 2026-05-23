package com.app.mobile.domain.mappers

import com.app.mobile.data.api.models.ApiResult
import com.app.mobile.data.api.models.HttpCode
import com.app.mobile.data.api.mappers.toErrorMessage
import com.app.mobile.domain.models.confirmation.ConfirmationModel
import com.app.mobile.presentation.models.account.ConfirmationModelUi
import com.app.mobile.presentation.models.account.ConfirmationResultUi


fun ConfirmationModelUi.toDomain() = ConfirmationModel(
    email = email,
    code = code.filter { it != '-' }, // так не должно быть думаю что надо фиксить в UI
    type = typeConfirmationFormatter(type)
)

fun ApiResult<Unit>.toConfirmationUiModel() = when (this) {
    is ApiResult.Success -> ConfirmationResultUi.Success

    is ApiResult.HttpError -> when (code) {
        HttpCode.UNAUTHORIZED -> ConfirmationResultUi.Error(
            "Неверный код подтверждения или истек срок действия."
        )
        HttpCode.NOT_FOUND -> ConfirmationResultUi.Error(
            "Пользователь с таким email не зарегистрирован"
        )
        else -> ConfirmationResultUi.Error(toErrorMessage())
    }

    else -> ConfirmationResultUi.Error(toErrorMessage())
}