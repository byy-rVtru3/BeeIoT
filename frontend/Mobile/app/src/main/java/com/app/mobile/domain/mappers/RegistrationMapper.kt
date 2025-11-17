package com.app.mobile.domain.mappers

import com.app.mobile.domain.models.registration.RegistrationModel
import com.app.mobile.domain.models.registration.RegistrationRequestResult
import com.app.mobile.presentation.models.RegistrationModelUi
import com.app.mobile.presentation.models.RegistrationResultUi

fun RegistrationModel.toUiModel(repeatPassword: String = "") =
    RegistrationModelUi(
        email = email,
        name = name,
        password = password,
        repeatPassword = repeatPassword
    )

fun RegistrationRequestResult.toUiModel(): RegistrationResultUi {
    return when (this) {
        is RegistrationRequestResult.Success -> RegistrationResultUi.Success

        is RegistrationRequestResult.ServerError -> RegistrationResultUi.Error(
            "Ошибка сервера"
        )

        is RegistrationRequestResult.UserAlreadyExistsError -> RegistrationResultUi.Error(
            "Пользователь уже существует"
        )

        is RegistrationRequestResult.UnknownError -> RegistrationResultUi.Error(
            "Неизвестная ошибка"
        )

        is RegistrationRequestResult.TimeoutError -> RegistrationResultUi.Error(
            "Превышено время ожидания"
        )

        is RegistrationRequestResult.BadRequestError -> RegistrationResultUi.Error(
            "Некорректный запрос"
        )
    }
}